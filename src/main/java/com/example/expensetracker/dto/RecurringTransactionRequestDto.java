package com.example.expensetracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RecurringTransactionRequestDto(
        @NotNull(message = "{recurring.transaction.request.amount.not.null}")
        @Positive(message = "{recurring.transaction.request.amount.positive}")
        BigDecimal amount,
        
        @NotBlank(message = "{recurring.transaction.request.description.not.blank}")
        String description,
        
        @NotNull(message = "{recurring.transaction.request.category.id.not.null}")
        Long categoryId,
        
        @Min(1)
        int intervalDays
) {
}
