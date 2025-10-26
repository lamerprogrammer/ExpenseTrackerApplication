package com.example.expensetracker.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class DateRangeDto {
    
    @NotNull(message = "{date.range.dto.from.not.null}")
    private Instant from;
    
    @NotNull(message = "{date.range.dto.to.not.null}")
    private Instant to;
    
    @AssertTrue(message = "{date.range.dto.from.is.valid.range}")
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
