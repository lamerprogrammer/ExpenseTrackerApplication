package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpensesReportDto(BigDecimal total, List<CategorySumDto> byCategory) {
}
