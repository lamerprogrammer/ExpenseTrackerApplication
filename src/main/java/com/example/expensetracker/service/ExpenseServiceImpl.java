package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.CategorySumDto;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Month;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, UserRepository userRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public ExpensesReportDto getReport(UserDetailsImpl currentUser, Instant from, Instant to) {
        Long userId = currentUser.getDomainUser().getId();
        List<CategorySumDto> list = expenseRepository.sumByCategoryForUserBetween(userId, from, to);
        BigDecimal amount = expenseRepository.totalForUserBetween(userId, from, to);
        return new ExpensesReportDto(amount, list);
    }

    @Override
    @Transactional
    @CacheEvict(value = "monthlyReports", key = "T(java.util.Objects).hash(#currentUser.domainUser.id)")
    public Expense addExpense(UserDetailsImpl currentUser, Expense expense) {
        User user = userRepository.findById(currentUser.getDomainUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        expense.setUser(user);
        Expense saved = expenseRepository.save(expense);

        user.increaseTotalExpenses(expense.getAmount());
        userService.clearTotalExpensesCache(user.getId());
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = "monthlyReports", key = "T(java.util.Objects).hash(#currentUser.domainUser.id)")
    public void deleteExpense(UserDetailsImpl currentUser, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Расход не найден"));
        User user = expense.getUser();
        if (!user.getId().equals(currentUser.getDomainUser().getId())) {
            throw new SecurityException("Попытка удалить чужой расход");
        }

        user.decreaseTotalExpenses(expense.getAmount());
        userRepository.save(user);
        expenseRepository.delete(expense);
        userService.clearTotalExpensesCache(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "monthlyReports", key = "T(java.util.Objects).hash(#currentUser.domainUser.id, #month, #year)")
    public ExpensesReportDto getReportMonthly(Month month, Integer year, UserDetailsImpl currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        int y = (year == null) ? LocalDate.now().getYear() : year;
        int monthNumber = month.ordinal() + 1;

        ZoneId zone = ZoneId.systemDefault();
        LocalDate startDate = LocalDate.of(y, monthNumber, 1);
        LocalDate endDateInclusive = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Instant startInstant = startDate.atStartOfDay(zone).toInstant();
        Instant endInstant = endDateInclusive.plusDays(1).atStartOfDay(zone).toInstant().minusNanos(1);

        List<CategorySumDto> items = expenseRepository.getMonthlyReport(user, startInstant, endInstant);

        BigDecimal total = items.stream()
                .map(CategorySumDto::sum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExpensesReportDto(total, items);
    }
}
