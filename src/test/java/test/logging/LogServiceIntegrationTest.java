package test.logging;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.AppLogRepository;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.expensetracker.logging.AuditLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LogServiceIntegrationTest {

    @Autowired
    private LogService logService;

    @Autowired
    private ElasticsearchOperations esOps;

    @Test
    void findAll_shouldSaveAndReturnAllLogs() {
        Instant now = Instant.now();
        String email = "test-" + UUID.randomUUID() + "@example.com";
        LogEntry entry = createLogEntryTest(email);
        entry.setTimestamp(now);

        logService.log(entry);
        esOps.indexOps(AppLog.class).refresh();
        List<AppLog> logs = logService.findAll();

        assertThat(logs)
                .singleElement()
                .satisfies(log -> {
                    assertThat(log.getTimestamp()).isEqualTo(now);
                    assertThat(log.getLevel()).isEqualTo("INFO");
                    assertThat(log.getLogger()).isEqualTo("TestLogger");
                    assertThat(log.getMessage()).isEqualTo("Test message");
                    assertThat(log.getUserEmail()).isEqualTo(email);
                    assertThat(log.getEndPoint()).isEqualTo("/test/api");
                });
    }

    @Test
    void findByUserEmail_shouldSaveAndReturnLogsByMail() {
        Instant now = Instant.now();
        String email = "test-" + System.nanoTime() + "@example.com";
        LogEntry entry = createLogEntryTest(email);
        entry.setTimestamp(now);

        logService.log(entry);
        esOps.indexOps(AppLog.class).refresh();
        List<AppLog> logs = logService.findByUserEmail(email);

        assertThat(logs)
                .singleElement()
                .satisfies(log -> {
                    assertThat(log.getTimestamp()).isEqualTo(now);
                    assertThat(log.getLevel()).isEqualTo("INFO");
                    assertThat(log.getLogger()).isEqualTo("TestLogger");
                    assertThat(log.getMessage()).isEqualTo("Test message");
                    assertThat(log.getUserEmail()).isEqualTo(email);
                    assertThat(log.getEndPoint()).isEqualTo("/test/api");
                });
    }

    private LogEntry createLogEntryTest(String email) {
        LogEntry entry = new LogEntry();
        entry.setLevel(INFO);
        entry.setLogger("TestLogger");
        entry.setMessage("Test message");
        entry.setUser(email);
        entry.setPath("/test/api");
        return entry;
    }
}
