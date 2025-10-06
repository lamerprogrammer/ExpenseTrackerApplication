package com.example.expensetracker.logging.audit;

import java.time.Instant;
import java.util.Objects;

public final class AuditDto {

    private final Long id;
    private final AuditAction action;
    private final String targetUserEmail;
    private final String performedByEmail;
    private final Instant timeStamp;


    public AuditDto(Long id, AuditAction action, String targetUserEmail, String performedByEmail, Instant timeStamp) {
        this.id = id;
        this.action = action;
        this.targetUserEmail = targetUserEmail;
        this.performedByEmail = performedByEmail;
        this.timeStamp = timeStamp;
    }

    public Long getId() {
        return id;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getTargetUser() {
        return targetUserEmail;
    }

    public String getPerformedBy() {
        return performedByEmail;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public static AuditDto from(Audit entity) {
        AuditDto dto = new AuditDto(
                entity.getId(),
                entity.getAction(),
                entity.getTargetUser().getEmail(),
                entity.getPerformedBy().getEmail(),
                entity.getTimeStamp());
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
