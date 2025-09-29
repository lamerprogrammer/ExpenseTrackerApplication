package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;

@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String name = "newAdmin";
    private String email = UUID.randomUUID() + "@example.com";
    private String password = "pass";

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllUsers_shouldReturnListUsers_whenUsersExist() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.all.users")))
                .andExpect(jsonPath("$.path").value("/api/admin/users"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllUsersInPage_shouldReturnListUsers_whenUsersExist() throws Exception {
        createUser(email, Role.USER);
        mockMvc.perform(get("/api/admin/users/paged")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.all.users")))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].email").value(hasItem(email)));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(get("/api/admin/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.user.by.id")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId()))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getUserById_shouldThrowException_whenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", ID_INVALID))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message").value(msg("handle.user.not.found.by.id")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + ID_INVALID));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getUserById_shouldThrowException_whenIdInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("должно быть больше 0"))
                .andExpect(jsonPath("$.path").value("/api/admin/users/-1"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldUserBanned_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(put("/api/admin/users/{id}/ban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("ban.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/ban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditLogRepository.findAll()).isNotEmpty();
        assertThat(userRepository.findById(user.getId()).get().isBanned()).isTrue();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldThrowException_whenIdMatches() throws Exception {
        cleanDB();
        User user = createUser(ADMIN_EMAIL, Role.ADMIN);
        mockMvc.perform(put("/api/admin/users/{id}/ban", user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/ban"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldThrowException_whenUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/ban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldUserUnbanned_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(put("/api/admin/users/{id}/unban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("unban.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/unban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditLogRepository.findAll()).isNotEmpty();
        assertThat(userRepository.findById(user.getId()).get().isBanned()).isFalse();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/unban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void promoteUser_shouldAddRoleModerator_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(put("/api/admin/users/{id}/promote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("increase.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/promote"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }
    
    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void promoteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/promote", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void promoteUser_shouldReturnUser_whenUserRoleAlreadyExists() throws Exception {
        User user = createUser(email, Role.MODERATOR);
        mockMvc.perform(put("/api/admin/users/{id}/promote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("increase.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/promote"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void demoteUser_shouldAddRoleModerator_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(put("/api/admin/users/{id}/demote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("decrease.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/demote"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void demoteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/demote", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void demoteUser_shouldReturnUser_whenUserRoleAlreadyRemoved() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(put("/api/admin/users/{id}/demote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("decrease.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/demote"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }
    
    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldUserDelete_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", user.getId()))
                .andExpect(status().isOk());
        assertThat(auditLogRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldThrowException_whenIdMatches() throws Exception {
        cleanDB();
        User user = createUser(ADMIN_EMAIL, Role.ADMIN);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value("/api/admin/users/" + user.getId() + "/delete"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}/delete", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldCreateAdmin_whenEmailNotBusy() throws Exception {
        String jsonBody = getJsonBody();
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("create.admin")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS_CREATE_ADMINISTRATOR));
        assertThat(auditLogRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenAdminAlreadyExists() throws Exception {
        String jsonBody = getJsonBody();
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.exists")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenNameIsEmpty() throws Exception {
        String json = getJsonBody("", email, password);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.name.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenEmailIsEmpty() throws Exception {
        String json = getJsonBody(name, "", password);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenEmailInvalid() throws Exception {
        String json = getJsonBody(name, "not-email", password);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.invalid")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenPasswordIsEmpty() throws Exception {
        String json = getJsonBody(name, email, "");
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.password.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldCreateModer_whenEmailNotBusy() throws Exception {
        String jsonBody = getJsonBody();
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("create.moder")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS_CREATE_MODERATOR));
        assertThat(auditLogRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenAdminAlreadyExists() throws Exception {
        String jsonBody = getJsonBody();
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.exists")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenNameIsEmpty() throws Exception {
        String json = getJsonBody("", email, password);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.name.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenEmailIsEmpty() throws Exception {
        String json = getJsonBody(name, "", password);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenEmailInvalid() throws Exception {
        String json = getJsonBody(name, "not-email", password);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.invalid")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenPasswordIsEmpty() throws Exception {
        String json = getJsonBody(name, email, "");
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.password.not-blank")));
    }

    private String getJsonBody() throws JsonProcessingException {
        return objectMapper.writeValueAsString(new RegisterDto(name, email, password));
    }

    private String getJsonBody(String name, String email, String password) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new RegisterDto(name, email, password));
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    private User createUser(String mail, Role role) {
        User user = new User();
        user.setEmail(mail);
        user.setPassword(USER_PASSWORD);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    private void cleanDB() {
        jdbcTemplate.execute("TRUNCATE TABLE admin_audit CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    }
}
