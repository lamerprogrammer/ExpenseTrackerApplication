package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.logging.applog.AppLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import test.security.WithMockCustomUser;

import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.ADMIN_EMAIL;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AppLogControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AppLogService appLogService;

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getAllLogs_shouldReturnListLogs_whenLogsExist() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("logs.get.all")))
                .andExpect(jsonPath("$.path").value("/api/admin/logs"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockCustomUser(email = ADMIN_EMAIL, roles = {"ADMIN"})
    void getByUser_shouldReturnListLogs_whenUserExist() throws Exception {
        mockMvc.perform(get("/api/admin/logs/user/{email}", ADMIN_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("logs.get.by.user")))
                .andExpect(jsonPath("$.path").value("/api/admin/logs/user/" + ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }
}
