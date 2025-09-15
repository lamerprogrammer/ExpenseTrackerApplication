package test.logging;

import com.example.expensetracker.logging.AppLog;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.API_TEST_ENDPOINT;
import static test.util.Constants.USER_EMAIL;

public class AppLogTest {
    
    @Test
    void shouldCreateAppLogWithNoArgsConstructor() {
        AppLog appLog = new AppLog();
        
        assertThat(appLog).isNotNull();
    }

    @Test
    void shouldSetFieldsUsingConstructor() {
        AppLog applog = TestData.appLog();

        assertThat(applog.getId()).isEqualTo("42");
        assertThat(applog.getTimestamp()).isNotNull();
        assertThat(applog.getLevel()).isEqualTo("INFO");
        assertThat(applog.getLogger()).isEqualTo("TestData");
        assertThat(applog.getMessage()).isEqualTo("Test message");
        assertThat(applog.getUserEmail()).isEqualTo(USER_EMAIL);
        assertThat(applog.getEndPoint()).isEqualTo(API_TEST_ENDPOINT);
        assertThat(applog.getStackTrace()).isEqualTo("StackTrace");
    }

    @Test
    void shouldBuildAppLogUsingBuilder() {
        Instant now = Instant.now();
        AppLog appLog = AppLog.builder()
                .id("1")
                .timestamp(now)
                .level("INFO")
                .logger("AppLogTest")
                .message("Message")
                .userEmail(USER_EMAIL)
                .endPoint(API_TEST_ENDPOINT)
                .stackTrace("StackTrace")
                .build();

        assertThat(appLog.getId()).isEqualTo("1");
        assertThat(appLog.getTimestamp()).isEqualTo(now);
        assertThat(appLog.getLevel()).isEqualTo("INFO");
        assertThat(appLog.getLogger()).isEqualTo("AppLogTest");
        assertThat(appLog.getMessage()).isEqualTo("Message");
        assertThat(appLog.getUserEmail()).isEqualTo(USER_EMAIL);
        assertThat(appLog.getEndPoint()).isEqualTo(API_TEST_ENDPOINT);
        assertThat(appLog.getStackTrace()).isEqualTo("StackTrace");
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(AppLog.class)
                .withOnlyTheseFields("id", "timestamp", "level", "logger", "message", "userEmail", "endPoint", "stackTrace")
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }
}
