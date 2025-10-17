package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.model.Expense;

import java.time.Instant;

public interface ExpenseService {
    ExpensesReportDto getReport(UserDetailsImpl currentUser, Instant from, Instant to);
    Expense addExpense(UserDetailsImpl currentUser, Expense expense);
    void deleteExpense(UserDetailsImpl currentUser, Long expenseId);
}
