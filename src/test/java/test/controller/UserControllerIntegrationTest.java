package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;

@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void getCurrentUser_shouldReturnAuthenticatedUser_whenUserLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    @WithMockCustomUser(email = MODERATOR_EMAIL, roles = {"MODERATOR"})
    void getCurrentUser_shouldReturnAuthenticatedModerator_whenModeratorLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(MODERATOR_EMAIL));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getCurrentUser_shouldReturnAuthenticatedAdmin_whenAdminLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL));
    }

    @Test
    void getCurrentUser_shouldReturnUnauthorized_whenNoUserLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
