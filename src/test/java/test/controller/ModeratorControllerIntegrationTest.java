package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;
import test.util.TestData;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.MODERATOR_EMAIL;
import static test.util.Constants.USER_EMAIL;

@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ModeratorControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private MockMvc mockMvc;

    private User user;
    private User moderator;
    private User admin;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        user = TestData.user();
        user.setId(42L);
        moderator = TestData.moderator();
        moderator.setId(5L);
        admin = TestData.admin();
        admin.setId(1L);
        userRepository.save(user);
        userRepository.save(moderator);
        userRepository.save(admin);
    }

    @Test
    @WithMockCustomUser(email = MODERATOR_EMAIL, roles = {"MODERATOR"})
    void getAllUsers_shouldReturnList_whenModeratorAuthorized() throws Exception {
        mockMvc.perform(get("/api/mod/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void getAllUsers_shouldReturnForbidden_whenUserAuthorized() throws Exception {
        mockMvc.perform(get("/api/mod/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/mod/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(email = MODERATOR_EMAIL, roles = {"MODERATOR"})
    void banUser_shouldBanAndCreateAuditLog_whenModeratorAuthorized() throws Exception {
        mockMvc.perform(delete("/api/mod/users/{id}/", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(email = MODERATOR_EMAIL, roles = {"MODERATOR"})
    void banUser_shouldReturnForbidden_whenUserAuthorized() throws Exception {
        mockMvc.perform(delete("/api/mod/users/{id}/", user.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(email = MODERATOR_EMAIL, roles = {"MODERATOR"})
    void banUser_shouldReturnForbidden_when() throws Exception {
        mockMvc.perform(delete("/api/mod/users"))
                .andExpect(status().isForbidden());
    }
}
