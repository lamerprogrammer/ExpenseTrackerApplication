package com.example.expensetracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RecurringTransactionRequestDto(
        @NotNull(message = "{recurring.transaction.request.dto.amount.not.null}")
        @Positive(message = "{recurring.transaction.request.dto.amount.positive}")
        BigDecimal amount,
        
        @NotBlank(message = "{recurring.transaction.request.dto.description}")
        String description,
        
        @NotNull(message = "{recurring.transaction.request.dto.categoryId}")
        Long categoryId,
        
        @Min(1)
        int intervalDays
) {
}
