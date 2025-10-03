package com.example.expensetracker.logging.audit;

import java.time.Instant;
import java.util.Objects;

public class AuditDto {

    private Long id;
    private AuditAction action;
    private String targetUserEmail;
    private String performedByEmail;
    private Instant timeStamp;


    public AuditDto(Long id, AuditAction action, String targetUserEmail, String performedByEmail) {
        this.id = id;
        this.action = action;
        this.targetUserEmail = targetUserEmail;
        this.performedByEmail = performedByEmail;
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

    public String getTargetUser() {
        return targetUserEmail;
    }

    public void setTargetUser(String targetUser) {
        this.targetUserEmail = targetUser;
    }

    public String getPerformedBy() {
        return performedByEmail;
    }

    public void setPerformedBy(String performedBy) {
        this.performedByEmail = performedBy;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public static AuditDto from(Audit entity) {
        AuditDto dto = new AuditDto(entity.getId(),
                entity.getAction(),
                entity.getTargetUser().getEmail(),
                entity.getPerformedBy().getEmail());
        dto.setTimeStamp(entity.getTimeStamp());
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditDto auditDto = (AuditDto) o;
        return action == auditDto.action &&
                Objects.equals(targetUserEmail, auditDto.targetUserEmail) &&
                Objects.equals(performedByEmail, auditDto.performedByEmail) &&
                Objects.equals(timeStamp, auditDto.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, targetUserEmail, performedByEmail, timeStamp);
    }
}
