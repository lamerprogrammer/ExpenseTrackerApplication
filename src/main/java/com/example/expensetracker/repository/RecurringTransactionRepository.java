package com.example.expensetracker.repository;

import com.example.expensetracker.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findAllByNextExecutionDateLessThanEqual(LocalDate date);
    List<RecurringTransaction> findAllByUser_Email(String email);
}
