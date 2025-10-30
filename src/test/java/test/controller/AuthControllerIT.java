package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestMessageSource.msg;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    private String name;
    private String email;
    private String password;

    @BeforeEach
    void setUp() {
        name = UUID.randomUUID().toString();
        email = "test-" + UUID.randomUUID() + "@example.com";
        password = USER_PASSWORD;
    }

    @Test
    void fullFlow_register_login_refresh() throws Exception {
        RegisterDto registerDto = new RegisterDto(USER_NAME, email, password);
        mockMvc.perform(post(API_AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(email));

        LoginRequest loginRequest = new LoginRequest(email, password);
        String loginResponse = mockMvc.perform(post(API_AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponse).path("data").path("refreshToken").asText();
        assertThat(refreshToken).isNotBlank();

        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
        mockMvc.perform(post(API_AUTH_REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void register_shouldThrowException_whenUserAlreadyExist() throws Exception {
        RegisterDto registerDto = new RegisterDto(USER_NAME, email, password);
        mockMvc.perform(post(API_AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)));
        mockMvc.perform(post(API_AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(msg("handle.data.integrity.violation")));
    }

    @Test
    void register_shouldThrowException_whenEmailIsEmpty() throws Exception {
        RegisterDto registerDto = new RegisterDto(USER_NAME, "", password);
        mockMvc.perform(post(API_AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("user.email.not-blank")));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        mockMvc.perform(post(API_AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.bad.credentials")));
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() throws Exception {
        registerUser(name, email, password);

        LoginRequest loginRequest = new LoginRequest(email, "invalidPassword");
        mockMvc.perform(post(API_AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.bad.credentials")));
    }

    @Test
    void refresh_shouldThrowException_whenUserNotFound() throws Exception {
        String token = jwtUtil.generateRefreshToken(email);
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", token));
        mockMvc.perform(post(API_AUTH_REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.username.not.found")));
    }

    @Test
    void refresh_shouldReturnForbidden_whenUserIsBanned() throws Exception {
        registerUser(name, email, password);
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setBanned(true);
            userRepository.save(user);
        });
        String token = jwtUtil.generateRefreshToken(email);
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", token));
        mockMvc.perform(post(API_AUTH_REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(msg("handle.access.denied")));
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenTokenInvalid() throws Exception {
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", "invalidToken"));
        mockMvc.perform(post(API_AUTH_REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(msg("handle.bad.credentials")));
    }

    private void registerUser(String name, String email, String password) throws Exception {
        RegisterDto registerDto = new RegisterDto(name, email, password);
        mockMvc.perform(post(API_AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)));
    }
}
