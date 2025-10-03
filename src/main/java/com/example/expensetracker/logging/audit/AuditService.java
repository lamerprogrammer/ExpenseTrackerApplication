package com.example.expensetracker.logging.audit;

import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    public AuditDto logAction(AuditAction action, User target, User performedBy) {
        Audit audit = new Audit(action, target, performedBy);
        return AuditDto.from(auditRepository.save(audit));
    }

    public Page<AuditDto> getAll(Pageable pageable) {
        return auditRepository.findAll(pageable).map(AuditDto::from);
    }

    public Page<AuditDto> getByAdmin(Long adminId, Pageable pageable) {
        return auditRepository.findByPerformedBy_Id(adminId, pageable).map(AuditDto::from);
    }
}
