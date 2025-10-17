package com.example.expensetracker.dto;

import java.math.BigDecimal;

public record CategorySumDto(String categoryName, BigDecimal sum) {
}
