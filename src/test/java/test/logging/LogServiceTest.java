package test.logging;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.AppLogRepository;
import com.example.expensetracker.logging.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LogServiceTest {

    @Autowired
    private LogService logService;

    @Autowired
    private AppLogRepository appLogRepository;

    @Test
    public void shouldSaveLogInMemoryRepository() {
        logService.log("INFO", "Test message", "user@example.com", "/api/test");

        List<AppLog> logs = logService.findAll();

        assertThat(logs).hasSize(1).allSatisfy(log -> {
            assertThat(log.getLevel()).isEqualTo("INFO");
            assertThat(log.getMessage()).isEqualTo("Test message");
        });
    }
}
