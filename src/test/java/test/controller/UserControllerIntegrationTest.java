package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestUtils.cleanDB;
import static test.util.TestUtils.createUser;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanDB(jdbcTemplate);
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void getCurrentUser_shouldReturnAuthenticatedUser_whenUserLoggedIn() throws Exception {
        createUser(USER_EMAIL, Role.USER, userRepository);
        performMe(USER_EMAIL);
    }

    @Test
    @WithMockCustomUser(email = MODERATOR_EMAIL, roles = {"MODERATOR"})
    void getCurrentUser_shouldReturnAuthenticatedModerator_whenModeratorLoggedIn() throws Exception {
        createUser(MODERATOR_EMAIL, Role.MODERATOR, userRepository);
        performMe(MODERATOR_EMAIL);
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getCurrentUser_shouldReturnAuthenticatedAdmin_whenAdminLoggedIn() throws Exception {
        createUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        performMe(ADMIN_EMAIL);
    }

    @Test
    void getCurrentUser_shouldReturnUnauthorized_whenNoUserLoggedIn() throws Exception {
        mockMvc.perform(get(API_USERS_ME))
                .andExpect(status().isUnauthorized());
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    private void performMe(String email) throws Exception {
        mockMvc.perform(get(API_USERS_ME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.current.user")))
                .andExpect(jsonPath("$.path").value(API_USERS_ME))
                .andExpect(jsonPath("$.data.email").value(email));
    }
}
