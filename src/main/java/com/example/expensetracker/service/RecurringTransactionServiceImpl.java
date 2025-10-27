package com.example.expensetracker.service;

import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.mapper.RecurringTransactionMapper;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.RecurringTransaction;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.RecurringTransactionRepository;
import com.example.expensetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionServiceImpl.class);
    
    private final RecurringTransactionRepository recurringRepo;
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final RecurringTransactionMapper mapper;

    public RecurringTransactionServiceImpl(RecurringTransactionRepository recurringRepo, 
                                           ExpenseRepository expenseRepo, 
                                           UserRepository userRepo, 
                                           CategoryRepository categoryRepo, 
                                           RecurringTransactionMapper mapper) {
        this.recurringRepo = recurringRepo;
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.mapper = mapper;
    }

    @Transactional
    @Scheduled(cron = "${spring.scheduler.recurring-cron}", zone = "${spring.scheduler.zone}")
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringRepo
                .findAllByNextExecutionDateLessThanEqual(today);
        
        for (RecurringTransaction recurring : dueTransactions) {
            Expense expense = new Expense();
            expense.setAmount(recurring.getAmount());
            expense.setDescription(recurring.getDescription());
            expense.setCategory(recurring.getCategory());
            expense.setUser(recurring.getUser());
            expense.setOccurredAt(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

            expenseRepo.save(expense);
            
            recurring.setNextExecutionDate(today.plusDays(recurring.getIntervalDays()));
            recurringRepo.save(recurring);
            
            log.info("Создана повторяющаяся транзакция для пользователя {} на сумму {}", recurring.getUser().getEmail(),
                    recurring.getAmount());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionDto> getUserRecurringTransactions(UserDetails currentUser) {
        User user = userRepo.findByEmail(currentUser.getUsername()).orElseThrow(() ->
                new UserNotFoundByIdException("User not found"));
        List<RecurringTransaction> recurringTransactionList = recurringRepo.findAllByUser_Email(user.getEmail());
        return recurringTransactionList.stream().map(mapper::toDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RecurringTransactionDto createRecurringTransaction(UserDetails currentUser,
                                                              RecurringTransactionRequestDto dto) {
        User user = userRepo.findByEmail(currentUser.getUsername()).orElseThrow(() ->
                new UserNotFoundByIdException("User not found"));
        Category category = categoryRepo.findById(dto.categoryId()).orElseThrow(() ->
                new IllegalArgumentException("Category not found"));

        RecurringTransaction entity = mapper.fromRequest(dto, category);
        entity.setUser(user);
        entity.setActive(true);
        return mapper.toDto(recurringRepo.save(entity));
    }
    
    @Override
    @Transactional
    public RecurringTransactionDto toggleActive(Long id) {
        RecurringTransaction transaction = recurringRepo.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Recurring transaction not found"));
        transaction.setActive(!transaction.isActive());
        RecurringTransaction saved = recurringRepo.save(transaction);
        return mapper.toDto(saved);
    }
}
