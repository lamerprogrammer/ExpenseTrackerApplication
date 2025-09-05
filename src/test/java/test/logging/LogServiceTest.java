package test.logging;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.AppLogRepository;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LogServiceTest {

    @Autowired
    private LogService logService;

    @Autowired
    private AppLogRepository appLogRepository;

    @BeforeEach
    void cleanRepository() {
        appLogRepository.deleteAll();
    }

    @Test
    public void shouldSaveLogInMemoryRepository() {
        Instant now = Instant.now();
        LogEntry entry = new LogEntry();
        entry.setTimestamp(now);
        entry.setLevel("INFO");
        entry.setLogger("TestLogger");
        entry.setMessage("Test message");
        entry.setUser("User");
        entry.setPath("/test/api");
        entry.setStackTrace("StackTrace");

        logService.log(entry);
        List<AppLog> logs = logService.findAll();

        assertThat(logs).
                hasSize(1)
                .first()
                .satisfies(log -> {
                    assertThat(log.getTimestamp()).isEqualTo(now);
                    assertThat(log.getLevel()).isEqualTo("INFO");
                    assertThat(log.getLogger()).isEqualTo("TestLogger");
                    assertThat(log.getMessage()).isEqualTo("Test message");
                    assertThat(log.getUserEmail()).isEqualTo("User");
                    assertThat(log.getEndPoint()).isEqualTo("/test/api");
                    assertThat(log.getStackTrace()).isEqualTo("StackTrace");
                });
    }
}
