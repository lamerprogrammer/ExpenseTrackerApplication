package com.example.expensetracker.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "admin_audit")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "performed_by")
    private String performedBy;

    private Instant timeStamp = Instant.now();

    public AuditLog() {}

    public AuditLog(AuditAction action, Long targetUserId, String performedBy) {
        this.action = action;
        this.targetUserId = targetUserId;
        this.performedBy = performedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }
}
