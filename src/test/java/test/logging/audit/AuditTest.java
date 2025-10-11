package test.logging.audit;

import com.example.expensetracker.logging.audit.AuditAction;
import com.example.expensetracker.logging.audit.Audit;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static test.util.Constants.ADMIN_EMAIL;

public class AuditTest {

    @Test
    void constructor_shouldSetFieldsUsing() {
        Audit audit = new Audit(AuditAction.BAN, TestData.user(), TestData.admin());

        assertThat(audit.getAction()).isEqualTo(AuditAction.BAN);
        assertThat(audit.getTargetUser().getId()).isEqualTo(42L);
        assertThat(audit.getPerformedBy().getEmail()).isEqualTo(ADMIN_EMAIL);
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
        assertThat(audit.getPerformedBy()).extracting(User::getId, User::getEmail)
                .containsExactly(performedBy.getId(), performedBy.getEmail());
        assertThat(audit.getTimeStamp()).isEqualTo(now);
    }

    @Test
    void shouldSetDefaultIdAndTimeStamp() {
        Audit audit = new Audit();

        assertThat(audit.getId()).isNull();
        assertThat(audit.getTimeStamp()).isNotNull();
        assertThat(audit.getTimeStamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void shouldSetIdAndTimeStamp() throws Exception {
        Audit audit = new Audit();
        Method method = Audit.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(audit);

        assertThat(audit.getTimeStamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}
