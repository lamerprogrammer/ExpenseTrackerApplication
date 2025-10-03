package test.logging;

import com.example.expensetracker.logging.applog.AppLogDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.API_TEST_ENDPOINT;
import static test.util.Constants.USER_EMAIL;

public class AppLogDtoTest {

    @Test
    void shouldCreateAppLogWithNoArgsConstructor() {
        AppLogDto appLogDto = new AppLogDto();

        assertThat(appLogDto).isNotNull();
    }

    @Test
    void shouldSetFieldsUsingConstructor() {
        AppLogDto applog = TestData.appLogDto();

        assertThat(applog.getId()).isEqualTo("42");
        assertThat(applog.getTimestamp()).isNotNull();
        assertThat(applog.getLevel()).isEqualTo(INFO);
        assertThat(applog.getLogger()).isEqualTo("TestData");
        assertThat(applog.getMessage()).isEqualTo("Test message");
        assertThat(applog.getUserEmail()).isEqualTo(USER_EMAIL);
        assertThat(applog.getEndPoint()).isEqualTo(API_TEST_ENDPOINT);
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(AppLogDto.class)
                .withOnlyTheseFields("id", "timestamp", "level", "logger", "errorType", "message", "userEmail", "endPoint")
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
