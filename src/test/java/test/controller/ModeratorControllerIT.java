package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.logging.audit.AuditRepository;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestUtils.createUser;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ModeratorControllerIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String name = "newModer";
    private String email = UUID.randomUUID() + "@example.com";
    private String password = "pass";
    private Long idModerator;

    @BeforeEach
    void setUp() {
        User moderator = createUser(MODERATOR_EMAIL, Role.MODERATOR, userRepository);
        idModerator = moderator.getId();
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAllUsers_shouldReturnListUsers_whenUsersExist() throws Exception {
        createUser(email, Role.USER, userRepository);
        mockMvc.perform(get(API_MODERATOR_USERS)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.all.users")))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].email").value(hasItem(email)));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER, userRepository);
        mockMvc.perform(get(API_MODERATOR_USERS + "/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("get.user.by.id")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId())))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getUserById_shouldThrowException_whenUserNotExists() throws Exception {
        mockMvc.perform(get(API_MODERATOR_USERS + "/{id}", ID_INVALID))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message").value(msg("handle.user.not.found.by.id")))
                .andExpect(jsonPath("$.path").value(userPath(ID_INVALID)));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getUserById_shouldThrowException_whenIdInvalid() throws Exception {
        mockMvc.perform(get(API_MODERATOR_USERS + "/{id}", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("должно быть больше 0"))
                .andExpect(jsonPath("$.path").value(userPath(-1L)));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldUserBanned_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER, userRepository);
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("ban.user")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId()) + "/ban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isNotEmpty();
        AssertionsForClassTypes.assertThat(userRepository.findById(user.getId()).get().isBanned()).isTrue();
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldReturnOk_whenUserAlreadyBanned() throws Exception {
        User user = createUser(email, Role.USER, userRepository);
        user.setBanned(true);
        userRepository.save(user);
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("ban.user")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId()) + "/ban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isEmpty();
        AssertionsForClassTypes.assertThat(userRepository.findById(user.getId()).get().isBanned()).isTrue();
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldThrowException_whenUserIsModer() throws Exception {
        User user = createUser(email, Role.MODERATOR, userRepository);
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", user.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(msg("handle.access.denied")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId()) + "/ban"));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldThrowException_whenUserIsAdmin() throws Exception {
        User user = createUser(email, Role.ADMIN, userRepository);
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", user.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(msg("handle.access.denied")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId()) + "/ban"));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldThrowException_whenIdMatches() throws Exception {
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", idModerator))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value(userPath(idModerator) + "/ban"));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldThrowException_whenUserNotExists() throws Exception {
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void banUser_shouldThrowException_whenCurrentUserNotExists() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/ban", ID_INVALID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void unbanUser_shouldUserUnbanned_whenUserExists() throws Exception {
        User user = createUser(email, Role.USER, userRepository);
        user.setBanned(true);
        userRepository.save(user);
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/unban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("unban.user")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId()) + "/unban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isNotEmpty();
        AssertionsForClassTypes.assertThat(userRepository.findById(user.getId()).get().isBanned()).isFalse();
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void unbanUser_shouldReturnOk_whenUserAlreadyUnbanned() throws Exception {
        User user = createUser(email, Role.USER, userRepository);
        user.setBanned(false);
        userRepository.save(user);
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/unban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("unban.user")))
                .andExpect(jsonPath("$.path").value(userPath(user.getId()) + "/unban"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        assertThat(auditRepository.findAll()).isEmpty();
        AssertionsForClassTypes.assertThat(userRepository.findById(user.getId()).get().isBanned()).isFalse();
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void unbanUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/unban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.entity.not.found")));
    }

    @Test
    @WithUserDetails(value = MODERATOR_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void unbanUser_shouldThrowException_whenCurrentUserNotExists() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(put(API_MODERATOR_USERS + "/{id}/unban", ID_INVALID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private String userPath(Long id) {
        return API_MODERATOR_USERS + "/" + id;
    }
}
