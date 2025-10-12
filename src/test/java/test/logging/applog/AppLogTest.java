package test.logging.applog;

import com.example.expensetracker.logging.applog.AppLog;
import com.example.expensetracker.logging.applog.AppLogDto;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static test.util.Constants.*;
import static test.util.Constants.TYPE_ERROR_WARN;

public class AppLogTest {

    @Test
    void shouldCreateAppLogWithNoArgsConstructor() {
        AppLog appLog = new AppLog();

        assertThat(appLog).isNotNull();
    }

    @Test
    void shouldSetFieldsUsingConstructor() {
        AppLog applog = new AppLog();
        applog.setId(ID_STRING);
        applog.setTimestamp(Instant.now());
        applog.setLevel(INFO);
        applog.setLogger(LOGGER_TEST_DATA);
        applog.setMessage(TEST_MESSAGE);
        applog.setUserEmail(USER_EMAIL);
        applog.setEndPoint(API_TEST_ENDPOINT);
        applog.setErrorType(TYPE_ERROR_WARN);

        assertThat(applog.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(applog).extracting(AppLog::getId, AppLog::getLevel, AppLog::getLogger,
                        AppLog::getMessage, AppLog::getUserEmail, AppLog::getEndPoint, AppLog::getErrorType)
                .containsExactly(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN);
    }

    @Test
    void from_shouldMapAllFieldsCorrectly() {
        AppLogDto appLogDto = TestData.appLogDto();

        AppLog result = AppLog.from(appLogDto);

        assertThat(result.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(result).extracting(AppLog::getId, AppLog::getLevel, AppLog::getLogger,
                        AppLog::getMessage, AppLog::getUserEmail, AppLog::getEndPoint, AppLog::getErrorType)
                .containsExactly(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN);
    }
}
