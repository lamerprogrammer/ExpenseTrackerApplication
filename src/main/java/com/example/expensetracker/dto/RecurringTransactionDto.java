package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class RecurringTransactionDto {

    private final Long id;

    private final BigDecimal amount;

    private final String description;

    private final Long categoryId;

    private final String categoryName;

    private final int intervalDays;

    private final LocalDate nextExecutionDate;

    private final boolean active;

    public RecurringTransactionDto(Long id, BigDecimal amount, String description, Long categoryId,
                                   String categoryName, int intervalDays, LocalDate nextExecutionDate, boolean active) {
        if (amount == null || description == null || categoryId == null || nextExecutionDate == null) {
            throw new IllegalArgumentException("RecurringTransactionDto: mandatory field is null");
        }
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.intervalDays = intervalDays;
        this.nextExecutionDate = nextExecutionDate;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }

    public boolean isActive() {
        return active;
    }
}
