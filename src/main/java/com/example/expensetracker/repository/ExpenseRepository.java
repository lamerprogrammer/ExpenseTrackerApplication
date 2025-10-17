package com.example.expensetracker.repository;

import com.example.expensetracker.dto.CategorySumDto;
import com.example.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("""
    SELECT new com.example.expensetracker.dto.CategorySumDto(
    COALESCE(c.name, 'UNCATEGORIZED'), SUM(e.amount))
    FROM Expense e LEFT JOIN e.category c
    WHERE e.user.id = :userId AND e.occurredAt
    BETWEEN :from AND :to GROUP BY c.name""")
    List<CategorySumDto> sumByCategoryForUserBetween(Long userId, Instant from, Instant to);
    
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.occurredAt " +
            "BETWEEN :from AND :to")
    BigDecimal totalForUserBetween(Long userId, Instant from, Instant to);
}
