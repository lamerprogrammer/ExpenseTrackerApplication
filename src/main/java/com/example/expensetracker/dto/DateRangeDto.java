package com.example.expensetracker.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class DateRangeDto {
    
    @NotNull
    private Instant from;
    
    @NotNull
    private Instant to;
    
    @AssertTrue(message = "from должен быть раньше to")
    public boolean isValidRange() {
        return from != null && to != null && from.isBefore(to);
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }
}
