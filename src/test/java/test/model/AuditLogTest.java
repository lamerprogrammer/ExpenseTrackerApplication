package test.model;

import com.example.expensetracker.model.AuditAction;
import com.example.expensetracker.model.AuditLog;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditLogTest {

    @Test
    void shouldSetFieldsUsingConstructor() {
        AuditLog auditLog = new AuditLog(AuditAction.BAN, TestData.user(), TestData.admin());

        assertThat(auditLog.getAction()).isEqualTo(AuditAction.BAN);
        assertThat(auditLog.getTargetUser()).isEqualTo(42L);
        assertThat(auditLog.getPerformedBy()).isEqualTo("admin");
    }

    @Test
    void setters_shouldSetAllFieldsCorrectly() {
        AuditLog auditLog = new AuditLog();
        Instant now = Instant.now();
        User targetUser = TestData.user();
        User performedBy = TestData.admin();

        auditLog.setId(1L);
        auditLog.setAction(AuditAction.UNBAN);
        auditLog.setTargetUser(targetUser);
        auditLog.setPerformedBy(performedBy);
        auditLog.setTimeStamp(now);

        assertThat(auditLog.getId()).isEqualTo(1L);
        assertThat(auditLog.getAction()).isEqualTo(AuditAction.UNBAN);
        assertThat(auditLog.getTargetUser()).extracting(User::getId, User::getEmail)
                .containsExactly(targetUser.getId(), targetUser.getEmail());
        assertThat(auditLog.getTargetUser()).extracting(User::getId, User::getEmail)
                .containsExactly(performedBy.getId(), performedBy.getEmail());
        assertThat(auditLog.getTimeStamp()).isEqualTo(now);
    }

    @Test
    void shouldSetDefaultIdAndTimeStamp() {
        AuditLog auditLog = new AuditLog();

        assertThat(auditLog.getId()).isNull();
        assertThat(auditLog.getTimeStamp()).isNotNull();
    }
}
