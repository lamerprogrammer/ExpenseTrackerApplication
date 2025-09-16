package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.dto.LoginDto;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.PASSWORD;

@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

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
        password = PASSWORD;
    }

    @Test
    void fullFlow_register_login_refresh() throws Exception {
        RegisterDto registerDto = new RegisterDto("Test User", email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        LoginDto loginRequest = new LoginDto(email, password);
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        assertThat(refreshToken).isNotBlank();
        
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
    
    @Test
    void register_shouldThrowException_whenUserAlreadyExist() throws Exception {
        RegisterDto registerDto = new RegisterDto("Test User", email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() throws Exception {
        LoginDto loginRequest = new LoginDto(email, password);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() throws Exception {
        registerUser(name, email, password);

        LoginDto loginRequest = new LoginDto(email, "invalidPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void refresh_shouldThrowException_whenUserNotFound() throws Exception {
        String token = jwtUtil.generateRefreshToken(email);
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", token));
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturnForbidden_whenUserIsBanned() throws Exception {// О Б Р А З Е Ц
        registerUser(name, email, password);
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setBanned(true);
            userRepository.save(user);
        });
        String token = jwtUtil.generateRefreshToken(email);
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", token));
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenTokenInvalid() throws Exception {
        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", "invalidToken"));
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized());
    }
    
    private void registerUser(String name, String email, String password) throws Exception {
        RegisterDto registerDto = new RegisterDto(name, email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)));
    }

    private void loginUser(String email, String password) throws Exception {
        LoginDto loginRequest = new LoginDto(email, password);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));
    }
}
