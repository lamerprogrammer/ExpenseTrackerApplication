package com.example.expensetracker.service;

import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface RecurringTransactionService {

    void processRecurringTransactions();
    List<RecurringTransactionDto> getUserRecurringTransactions(UserDetails user);
    RecurringTransactionDto createRecurringTransaction(UserDetails user,
                                                       RecurringTransactionRequestDto recurringTransaction);
    RecurringTransactionDto toggleActive(Long id);
}

