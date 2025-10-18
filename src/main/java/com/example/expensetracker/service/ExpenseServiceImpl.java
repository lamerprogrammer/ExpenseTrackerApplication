package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.CategorySumDto;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
}
