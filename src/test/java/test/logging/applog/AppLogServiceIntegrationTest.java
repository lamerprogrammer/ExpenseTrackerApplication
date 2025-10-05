//package test.logging;
//
//import com.example.expensetracker.ExpenseTrackerApplication;
//import com.example.expensetracker.config.TestBeansConfig;
//import com.example.expensetracker.logging.applog.AppLogDto;
//import com.example.expensetracker.logging.audit.AuditDto;
//import com.example.expensetracker.logging.applog.AppLogService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class AppLogServiceIntegrationTest {
//
//    @Autowired
//    private AppLogService appLogService;
//
//    @Autowired
//    private ElasticsearchOperations esOps;
//
//    @Test
//    void findAll_shouldSaveAndReturnAllPagedLogs() {
//        Instant now = Instant.now();
//        String email = "test-" + UUID.randomUUID() + "@example.com";
//        AuditDto entry = createLogEntryTest(email);
//        entry.setTimestamp(now);
//
//        appLogService.log(entry);
//        esOps.indexOps(AppLogDto.class).refresh();
//        List<AppLogDto> logs = appLogService.findAllPaged();
//
//        assertThat(logs)
//                .singleElement()
//                .satisfies(log -> {
//                    assertThat(log.getTimestamp()).isEqualTo(now);
//                    assertThat(log.getLevel()).isEqualTo("INFO");
//                    assertThat(log.getLogger()).isEqualTo("TestLogger");
//                    assertThat(log.getMessage()).isEqualTo("Test message");
//                    assertThat(log.getUserEmail()).isEqualTo(email);
//                    assertThat(log.getEndPoint()).isEqualTo("/test/api");
//                });
//    }
//
//    @Test
//    void findByUserEmail_shouldSaveAndReturnLogsByMail() {
//        Instant now = Instant.now();
//        String email = "test-" + System.nanoTime() + "@example.com";
//        AuditDto entry = createLogEntryTest(email);
//        entry.setTimestamp(now);
//
//        appLogService.log(entry);
//        esOps.indexOps(AppLogDto.class).refresh();
//        List<AppLogDto> logs = appLogService.findByUserEmail(email);
//
//        assertThat(logs)
//                .singleElement()
//                .satisfies(log -> {
//                    assertThat(log.getTimestamp()).isEqualTo(now);
//                    assertThat(log.getLevel()).isEqualTo("INFO");
//                    assertThat(log.getLogger()).isEqualTo("TestLogger");
//                    assertThat(log.getMessage()).isEqualTo("Test message");
//                    assertThat(log.getUserEmail()).isEqualTo(email);
//                    assertThat(log.getEndPoint()).isEqualTo("/test/api");
//                });
//    }
//
//    private AuditDto createLogEntryTest(String email) {
//        AuditDto entry = new AuditDto();
//        entry.setLevel(INFO);
//        entry.setLogger("TestLogger");
//        entry.setMessage("Test message");
//        entry.setUser(email);
//        entry.setPath("/test/api");
//        return entry;
//    }
//}
