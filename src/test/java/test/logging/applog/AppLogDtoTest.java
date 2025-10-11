package test.logging.applog;

import com.example.expensetracker.logging.applog.AppLog;
import com.example.expensetracker.logging.applog.AppLogDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static test.util.Constants.*;

public class AppLogDtoTest {

    @Test
    void shouldCreateAppLogWithNoArgsConstructor() {
        AppLogDto appLogDto = new AppLogDto();

        assertThat(appLogDto).isNotNull();
    }

    @Test
    void shouldSetFieldsUsingConstructor() {
        AppLogDto applog = new AppLogDto();
        applog.setId(ID_STRING);
        applog.setTimestamp(Instant.now());
        applog.setLevel(INFO);
        applog.setLogger(LOGGER_TEST_DATA);
        applog.setMessage(TEST_MESSAGE);
        applog.setUserEmail(USER_EMAIL);
        applog.setEndPoint(API_TEST_ENDPOINT);
        applog.setErrorType(TYPE_ERROR_WARN);

        assertThat(applog.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(applog).extracting(AppLogDto::getId, AppLogDto::getLevel, AppLogDto::getLogger,
                        AppLogDto::getMessage, AppLogDto::getUserEmail, AppLogDto::getEndPoint, AppLogDto::getErrorType)
                .containsExactly(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN);
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(AppLogDto.class)
                .withOnlyTheseFields("id", "timestamp", "level", "logger", "errorType", "message", "userEmail", "endPoint")
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }

    @Test
    void from_shouldMapAllFieldsCorrectly() {
        AppLog appLog = TestData.appLog();

        AppLogDto result = AppLogDto.from(appLog);

        assertThat(result.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(result).extracting(AppLogDto::getId, AppLogDto::getLevel, AppLogDto::getLogger,
                        AppLogDto::getMessage, AppLogDto::getUserEmail, AppLogDto::getEndPoint, AppLogDto::getErrorType)
                .containsExactly(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN);
    }
}
