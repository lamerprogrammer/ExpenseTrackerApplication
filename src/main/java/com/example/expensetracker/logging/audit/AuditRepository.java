package com.example.expensetracker.logging.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    Page<Audit> findByPerformedBy_Id(Long adminId, Pageable pageable);
}
