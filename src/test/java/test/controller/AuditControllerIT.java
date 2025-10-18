package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.logging.audit.AuditAction;
import com.example.expensetracker.logging.audit.AuditRepository;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;
import test.util.TestData;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.ADMIN_EMAIL;
import static test.util.Constants.USER_PASSWORD;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuditControllerIT {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditRepository auditRepository;

    private String email1;
    private String email2;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllAudit_shouldReturnListLogs_whenLogsExist() throws Exception {
        email1 = UUID.randomUUID() + "@example.com";
        email2 = UUID.randomUUID() + "@example.com";
        User admin = TestData.user(null, email1, USER_PASSWORD, Set.of(Role.ADMIN), false);
        User user = TestData.user(null, email2, USER_PASSWORD, Set.of(Role.USER), false);
        userRepository.save(admin);
        userRepository.save(user);
        auditService.logAction(AuditAction.BAN, user, admin);
        mockMvc.perform(get("/api/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("audit.get.all")))
                .andExpect(jsonPath("$.path").value("/api/admin/audit"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getByAdmin_shouldReturnListLogs_whenAdminExists() throws Exception {
        email1 = UUID.randomUUID() + "@example.com";
        email2 = UUID.randomUUID() + "@example.com";
        User admin = TestData.user(null, email1, USER_PASSWORD, Set.of(Role.ADMIN), false);
        User moderator = TestData.user(null, email2, USER_PASSWORD, Set.of(Role.MODERATOR), false);
        userRepository.save(admin);
        userRepository.save(moderator);
        auditService.logAction(AuditAction.CREATE, moderator, admin);
        mockMvc.perform(get("/api/admin/audit/{id}", admin.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("audit.get.by.admin")))
                .andExpect(jsonPath("$.path").value("/api/admin/audit/" + admin.getId()))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllAudit_shouldReturnListLogs_whenSortedSuccess() throws Exception {
        email1 = UUID.randomUUID() + "@example.com";
        email2 = UUID.randomUUID() + "@example.com";
        User admin = TestData.user(null, email1, USER_PASSWORD, Set.of(Role.ADMIN), false);
        User user = TestData.user(null, email2, USER_PASSWORD, Set.of(Role.USER), false);
        userRepository.save(admin);
        userRepository.save(user);
        auditService.logAction(AuditAction.BAN, user, admin);
        Thread.sleep(100);
        auditService.logAction(AuditAction.UNBAN, user, admin);
        mockMvc.perform(get("/api/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("audit.get.all")))
                .andExpect(jsonPath("$.path").value("/api/admin/audit"))
                .andExpect(jsonPath("$.data.content[0].action").value("UNBAN"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getByAdmin_shouldReturnListLogs_whenSortedSuccess() throws Exception {
        email1 = UUID.randomUUID() + "@example.com";
        email2 = UUID.randomUUID() + "@example.com";
        User admin = TestData.user(null, email1, USER_PASSWORD, Set.of(Role.ADMIN), false);
        User moderator = TestData.user(null, email2, USER_PASSWORD, Set.of(Role.MODERATOR), false);
        userRepository.save(admin);
        userRepository.save(moderator);
        auditService.logAction(AuditAction.CREATE, moderator, admin);
        Thread.sleep(100);
        auditService.logAction(AuditAction.DELETE, moderator, admin);
        mockMvc.perform(get("/api/admin/audit/{id}", admin.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("audit.get.by.admin")))
                .andExpect(jsonPath("$.path").value("/api/admin/audit/" + admin.getId()))
                .andExpect(jsonPath("$.data.content[0].action").value("DELETE"));
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
