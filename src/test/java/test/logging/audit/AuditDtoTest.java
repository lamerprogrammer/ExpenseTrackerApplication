package test.logging.audit;

import com.example.expensetracker.logging.audit.Audit;
import com.example.expensetracker.logging.audit.AuditDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.expensetracker.logging.audit.AuditAction.BAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static test.util.Constants.*;

public class AuditDtoTest {

    @Test
    void constructor_shouldSetAllFieldsCorrectly() {
        Instant now = Instant.now();
        AuditDto dto = new AuditDto(ID_VALID, BAN, USER_EMAIL, ADMIN_EMAIL, now);

        assertThat(dto.getTimeStamp()).isEqualTo(now);
        assertThat(dto.getId()).isEqualTo(ID_VALID);
        assertThat(dto.getAction()).isEqualTo(BAN);
        assertThat(dto.getTargetUser()).isEqualTo(USER_EMAIL);
        assertThat(dto.getPerformedBy()).isEqualTo(ADMIN_EMAIL);
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(AuditDto.class)
                .withOnlyTheseFields("action", "targetUserEmail", "performedByEmail", "timeStamp")
                .usingGetClass()
                .verify();
    }

    @Test
    void from_shouldReturnEntity() {
        Audit audit = new Audit(BAN, TestData.user(), TestData.admin());
        audit.setId(1L);
        audit.setTimeStamp(Instant.now());

        var dto = AuditDto.from(audit);

        assertThat(dto.getId()).isEqualTo(audit.getId());
        assertThat(dto.getAction()).isEqualTo(BAN);
        assertThat(dto.getTargetUser()).isEqualTo(audit.getTargetUser().getEmail());
        assertThat(dto.getPerformedBy()).isEqualTo(audit.getPerformedBy().getEmail());
        assertThat(dto.getTimeStamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}
