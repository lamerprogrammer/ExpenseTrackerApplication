package com.example.expensetracker.dto;

import java.math.BigDecimal;

public record RecurringTransactionRequestDto(
        BigDecimal amount,
        String description,
        Long categoryId,
        int intervalDays
) {
}
