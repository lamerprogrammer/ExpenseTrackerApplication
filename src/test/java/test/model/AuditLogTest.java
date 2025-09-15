package test.model;

import com.example.expensetracker.model.AuditAction;
import com.example.expensetracker.model.AuditLog;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditLogTest {

    @Test
    void shouldSetFieldsUsingConstructor() {
        AuditLog auditLog = new AuditLog(AuditAction.BAN, 42L, "admin");

        assertThat(auditLog.getAction()).isEqualTo(AuditAction.BAN);
        assertThat(auditLog.getTargetUserId()).isEqualTo(42L);
        assertThat(auditLog.getPerformedBy()).isEqualTo("admin");
    }

    @Test
    void setters_shouldSetAllFieldsCorrectly() {
        AuditLog auditLog = new AuditLog();
        Instant now = Instant.now();

        auditLog.setId(1L);
        auditLog.setAction(AuditAction.UNBAN);
        auditLog.setTargetUserId(42L);
        auditLog.setPerformedBy("admin");
        auditLog.setTimeStamp(now);

        assertThat(auditLog.getId()).isEqualTo(1L);
        assertThat(auditLog.getAction()).isEqualTo(AuditAction.UNBAN);
        assertThat(auditLog.getTargetUserId()).isEqualTo(42L);
        assertThat(auditLog.getPerformedBy()).isEqualTo("admin");
        assertThat(auditLog.getTimeStamp()).isEqualTo(now);
    }

    @Test
    void shouldSetDefaultIdAndTimeStamp() {
        AuditLog auditLog = new AuditLog();

        assertThat(auditLog.getId()).isNull();
        assertThat(auditLog.getTimeStamp()).isNotNull();
    }
}
