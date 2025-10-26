package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestMessageSource.msg;
import static test.util.TestUtils.createUser;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ExpenseControllerIT {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        createUser(USER_EMAIL, Role.USER, userRepository);
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void report_shouldReturnExpensesForPeriod() throws Exception {
        String from = "2025-09-01T00:00:00Z";
        String to = "2025-10-01T00:00:00Z";

        mockMvc.perform(get(API_EXPENSES_REPORT)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("expense.controller.report.ok")));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void report_shouldReturnBadRequest_whenInvalidDates() throws Exception {
        String invalidFrom = "2026-09-01T00:00:00Z";
        String invalidTo = "2024-10-01T00:00:00Z";

        mockMvc.perform(get(API_EXPENSES_REPORT)
                        .param("from", invalidFrom)
                        .param("to", invalidTo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getTotal_shouldReturnTotalExpenses() throws Exception {
        mockMvc.perform(get(API_EXPENSES_TOTAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("expense.controller.total.ok")));
    }
}
