package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.config.TestBeansConfig;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;

@SpringBootTest(classes = {ExpenseTrackerApplication.class, TestBeansConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllUsers_shouldReturnListUsers_whenUsersExist() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@example.com"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldUserBanned_whenUserExists() throws Exception {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword(USER_PASSWORD);
        userRepository.save(user);
        mockMvc.perform(put("/api/admin/users/{id}/ban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Пользователь " + user.getEmail() + " заблокирован"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void banUser_shouldThrowException_whenUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/ban", ID_INVALID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldUserUnbanned_whenUserExists() throws Exception {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword(USER_PASSWORD);
        userRepository.save(user);
        mockMvc.perform(put("/api/admin/users/{id}/unban", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Пользователь " + user.getEmail() + " разблокирован"));
        ;
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void unbanUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/unban", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Запрошенный ресурс не найден"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldUserDelete_whenUserExists() throws Exception {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword(USER_PASSWORD);
        userRepository.save(user);
        mockMvc.perform(delete("/api/admin/users/{id}/delete", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void deleteUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}/delete", ID_INVALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Запрошенный ресурс не найден"));
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldUserDelete_whenUserExists() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        RegisterDto newAdmin = new RegisterDto("newAdmin", email, "pass");
        String jsonBody = objectMapper.writeValueAsString(newAdmin);
        mockMvc.perform(post("/api/admin/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void createAdmin_shouldThrowException_whenAdminAlreadyExists() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        RegisterDto newAdmin = new RegisterDto("newAdmin", email, "pass");
        String jsonBody = objectMapper.writeValueAsString(newAdmin);
        mockMvc.perform(post("/api/admin/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());
    }
}
