package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestUtils.createUser;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void getCurrentUser_shouldReturnAuthenticatedAdmin_whenUserNotFound() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(get(API_USERS_ME))
                .andExpect(status().isUnauthorized())//если только этот тест запускаю, то он зелёный, а если все, то он красный Status expected:<401> but was:<200> 
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void changePassword_shouldReturnOk_whenAllFieldsValid() throws Exception {
        User user = createUser(USER_EMAIL, Role.USER, userRepository);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        ChangePasswordRequest req = new ChangePasswordRequest(USER_PASSWORD, "newPass");
        mockMvc.perform(put(API_USERS_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("user.controller.password.changed.success")));
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void changePassword_shouldThrowException_whenPasswordNotMatches() throws Exception {
        User user = createUser(USER_EMAIL, Role.USER, userRepository);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        ChangePasswordRequest req = new ChangePasswordRequest("invalidPassword", "newPass");
        mockMvc.perform(put(API_USERS_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.bad.credentials")));
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void changePassword_shouldThrowException_whenOldPasswordEmpty() throws Exception {
        createUser(USER_EMAIL, Role.USER, userRepository);
        ChangePasswordRequest req = new ChangePasswordRequest("", "newPass");
        mockMvc.perform(put(API_USERS_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.controller.user.password.old.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void changePassword_shouldThrowException_whenNewPasswordEmpty() throws Exception {
        User user = createUser(USER_EMAIL, Role.USER, userRepository);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        ChangePasswordRequest req = new ChangePasswordRequest(USER_PASSWORD, "");
        mockMvc.perform(put(API_USERS_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.controller.user.password.new.not-blank")));
    }

    @Test
    @WithMockCustomUser(email = USER_EMAIL, roles = {"USER"})
    void changePassword_shouldThrowException_whenUserNotFound() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest(USER_PASSWORD, USER_PASSWORD_NEW);
        mockMvc.perform(put(API_USERS_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    private void performMe(String email) throws Exception {
        mockMvc.perform(get(API_USERS_ME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("user.controller.get.current.user")))
                .andExpect(jsonPath("$.path").value(API_USERS_ME))
                .andExpect(jsonPath("$.data.email").value(email));
    }
}
