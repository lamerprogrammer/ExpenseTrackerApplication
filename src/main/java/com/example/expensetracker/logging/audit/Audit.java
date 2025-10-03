package com.example.expensetracker.logging.audit;

import com.example.expensetracker.model.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "admin_audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    private Instant timeStamp = Instant.now();

    public Audit() {
    }

    public Audit(AuditAction action, User targetUser, User performedBy) {
        this.action = action;
        this.targetUser = targetUser;
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

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    @PrePersist
    protected void onCreate() {
        this.timeStamp = Instant.now();
    }

    public static AuditDto from(Audit entity) {
        return new AuditDto(entity.getId(), entity.getAction(), entity.getTargetUser().getEmail(),
                entity.getPerformedBy().getEmail());
    }
}
