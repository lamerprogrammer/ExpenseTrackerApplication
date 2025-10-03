package com.example.expensetracker.logging.audit;

import com.example.expensetracker.logging.applog.AppLog;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogLevel;
import com.example.expensetracker.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Objects;

public class AuditDto {
    
    private Long id;    
    private AuditAction action;
    private User targetUser;
    private User performedBy;
    private Instant timeStamp;


    public AuditDto(Long id, AuditAction action, User targetUser, User performedBy) {
        this.id = id;
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

    public static AuditDto from(Audit entity) {
        return new AuditDto(entity.getId(), entity.getAction(), entity.getTargetUser(), entity.getPerformedBy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditDto auditDto = (AuditDto) o;
        return Objects.equals(id, auditDto.id) && 
                action == auditDto.action && 
                Objects.equals(targetUser, auditDto.targetUser) &&
                Objects.equals(performedBy, auditDto.performedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, action, targetUser, performedBy);
    }
}
