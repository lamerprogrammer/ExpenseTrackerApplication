package test.model;

import com.example.expensetracker.logging.audit.AuditAction;
import com.example.expensetracker.logging.audit.Audit;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditTest {

    @Test
    void shouldSetFieldsUsingConstructor() {
        Audit audit = new Audit(AuditAction.BAN, TestData.user(), TestData.admin());

        assertThat(audit.getAction()).isEqualTo(AuditAction.BAN);
        assertThat(audit.getTargetUser()).isEqualTo(42L);
        assertThat(audit.getPerformedBy()).isEqualTo("admin");
    }

    @Test
    void setters_shouldSetAllFieldsCorrectly() {
        Audit audit = new Audit();
        Instant now = Instant.now();
        User targetUser = TestData.user();
        User performedBy = TestData.admin();

        audit.setId(1L);
        audit.setAction(AuditAction.UNBAN);
        audit.setTargetUser(targetUser);
        audit.setPerformedBy(performedBy);
        audit.setTimeStamp(now);

        assertThat(audit.getId()).isEqualTo(1L);
        assertThat(audit.getAction()).isEqualTo(AuditAction.UNBAN);
        assertThat(audit.getTargetUser()).extracting(User::getId, User::getEmail)
                .containsExactly(targetUser.getId(), targetUser.getEmail());
        assertThat(audit.getTargetUser()).extracting(User::getId, User::getEmail)
                .containsExactly(performedBy.getId(), performedBy.getEmail());
        assertThat(audit.getTimeStamp()).isEqualTo(now);
    }

    @Test
    void shouldSetDefaultIdAndTimeStamp() {
        Audit audit = new Audit();

        assertThat(audit.getId()).isNull();
        assertThat(audit.getTimeStamp()).isNotNull();
    }
}
