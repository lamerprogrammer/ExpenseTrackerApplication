package com.example.expensetracker.logging.audit;

import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.example.expensetracker.logging.audit.AuditAction.CHANGE_PASSWORD;

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

    public void logPasswordChange(User user) {
        Audit audit = new Audit(CHANGE_PASSWORD, user, user);
        audit.setTimeStamp(Instant.now());
        auditRepository.save(audit);
    }
}
