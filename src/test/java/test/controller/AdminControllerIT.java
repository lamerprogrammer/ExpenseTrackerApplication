package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.logging.audit.AuditRepository;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestUtils.createAndSaveUser;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllUsers_shouldReturnListUsers_whenUsersExist() throws Exception {
        createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(get(API_ADMIN_USERS)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.all.users")))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].email").value(hasItem(USER_EMAIL)));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(get("/api/admin/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.user.by.id")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId()))
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
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + ID_INVALID));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getUserById_shouldThrowException_whenIdInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/-1"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldUserBanned_whenUserExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/ban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("ban.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/ban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isNotEmpty();
        assertThat(userRepository.findById(user.getId()).get().isBanned()).isTrue();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldReturnUser_whenUserAlreadyBanned() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        user.setBanned(true);
        userRepository.save(user);
        mockMvc.perform(put("/api/admin/users/{id}/ban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("ban.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/ban"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldThrowException_whenIdMatches() throws Exception {
        User user = createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/ban", user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/ban"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldThrowException_whenUserNotExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/ban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldUserUnbanned_whenUserExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        user.setBanned(true);
        userRepository.save(user);
        mockMvc.perform(put("/api/admin/users/{id}/unban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("unban.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/unban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isNotEmpty();
        assertThat(userRepository.findById(user.getId()).get().isBanned()).isFalse();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldReturnUser_whenUserAlreadyUnbanned() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        user.setBanned(false);
        userRepository.save(user);
        mockMvc.perform(put("/api/admin/users/{id}/unban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("unban.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/unban"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/unban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldReturnNotFound_whenCurrentUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/unban", ID_INVALID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void promoteUser_shouldAddRoleModerator_whenUserExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/promote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("promote.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/promote"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void promoteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/promote", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void promoteUser_shouldReturnUser_whenUserRoleAlreadyPromoted() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(MODERATOR_EMAIL, Role.MODERATOR, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/promote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("promote.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/promote"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void demoteUser_shouldAddRoleModerator_whenUserExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.MODERATOR, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/demote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("demote.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/demote"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void demoteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(put("/api/admin/users/{id}/demote", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void demoteUser_shouldReturnUser_whenRoleAlreadyDemoted() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(put(API_ADMIN_USERS + "/{id}/demote", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("demote.user")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/demote"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldUserDelete_whenUserExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", user.getId()))
                .andExpect(status().isOk());
        assertThat(auditRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldThrowException_whenCurrentUserNotExists() throws Exception {
        User user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", user.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldThrowException_whenIdMatches() throws Exception {
        User user = createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS + "/" + user.getId() + "/delete"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldCreateAdmin_whenEmailNotBusy() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        String jsonBody = getJsonBody(USER_NAME, "new" + ADMIN_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("create.admin")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS_CREATE_ADMINISTRATOR));
        assertThat(auditRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenCurrentUserNotExists() throws Exception {
        String jsonBody = getJsonBody(USER_NAME, "new" + ADMIN_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenAdminAlreadyExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        createAndSaveUser("new" + ADMIN_EMAIL, Role.ADMIN, userRepository);
        String jsonBody = getJsonBody(USER_NAME, "new" + ADMIN_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.exists")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenNameIsEmpty() throws Exception {
        String json = getJsonBody("", ADMIN_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.name.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenEmailIsEmpty() throws Exception {
        String json = getJsonBody(USER_NAME, "", USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenEmailInvalid() throws Exception {
        String json = getJsonBody(USER_NAME, "not-email", USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.invalid")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenPasswordIsEmpty() throws Exception {
        String json = getJsonBody(USER_NAME, ADMIN_EMAIL, "");
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_ADMINISTRATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.password.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldCreateModer_whenEmailNotBusy() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        String jsonBody = getJsonBody(USER_NAME, MODERATOR_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("create.moder")))
                .andExpect(jsonPath("$.path").value(API_ADMIN_USERS_CREATE_MODERATOR));
        assertThat(auditRepository.findAll()).isNotEmpty();
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenCurrentUserNotExists() throws Exception {
        String jsonBody = getJsonBody(USER_NAME, "new" + MODERATOR_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenAdminAlreadyExists() throws Exception {
        createAndSaveUser(ADMIN_EMAIL, Role.ADMIN, userRepository);
        createAndSaveUser(MODERATOR_EMAIL, Role.MODERATOR, userRepository);
        String jsonBody = getJsonBody(USER_NAME, MODERATOR_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.exists")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenNameIsEmpty() throws Exception {
        String json = getJsonBody("", MODERATOR_EMAIL, USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.name.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenEmailIsEmpty() throws Exception {
        String json = getJsonBody(USER_NAME, "", USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenEmailInvalid() throws Exception {
        String json = getJsonBody(USER_NAME, "not-email", USER_PASSWORD);
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.invalid")));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createModer_shouldThrowException_whenPasswordIsEmpty() throws Exception {
        String json = getJsonBody(USER_NAME, MODERATOR_EMAIL, "");
        mockMvc.perform(post(API_ADMIN_USERS_CREATE_MODERATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.password.not-blank")));
    }

    private String getJsonBody(String name, String email, String password) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new RegisterDto(name, email, password));
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
