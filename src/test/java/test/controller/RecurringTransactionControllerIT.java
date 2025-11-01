package test.controller;

import com.example.expensetracker.ExpenseTrackerApplication;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.RecurringTransaction;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.RecurringTransactionRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.RecurringTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static test.util.Constants.*;
import static test.util.TestMessageSource.msg;
import static test.util.TestUtils.createAndSaveUser;

@SpringBootTest(classes = {ExpenseTrackerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RecurringTransactionControllerIT {

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    User user;

    @BeforeEach
    void setUp() {
        user = createAndSaveUser(USER_EMAIL, Role.USER, userRepository);
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAll_shouldReturnEmptyList_whenNoTransactions() throws Exception {
        mockMvc.perform(get(API_RECURRING_TRANSACTION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.controller.get.all")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAll_shouldReturnListRecurringTransaction_whenTransactionsExist() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransaction recurringTransaction = new RecurringTransaction(new BigDecimal(AMOUNT), DESCRIPTION,
                category, user, INTERVAL_DAYS, LocalDate.now().minusDays(1));
        recurringTransactionRepository.save(recurringTransaction);
        mockMvc.perform(get(API_RECURRING_TRANSACTION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.controller.get.all")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].categoryId").value(category.getId()));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAll_shouldReturnNotFound_whenUserNotExists() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(get(API_RECURRING_TRANSACTION))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.user.not.found.by.id")));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnSuccess_whenAllFieldsValid() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT), DESCRIPTION,
                category.getId(), INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.controller.create")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE))
                .andExpect(jsonPath("$.data.amount").value(dto.amount()))
                .andExpect(jsonPath("$.data.categoryId").value(dto.categoryId()));
        assertThat(recurringTransactionRepository.findAll()).hasSize(1);
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnNotFound_whenUserNotExists() throws Exception {
        userRepository.delete(user);
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT), DESCRIPTION,
                category.getId(), INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(msg("handle.user.not.found.by.id")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnBadRequest_whenCategoryNotExists() throws Exception {
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT), DESCRIPTION,
                ID_INVALID, INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnBadRequest_whenAmountIsNegative() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT_NEGATIVE), 
                DESCRIPTION, category.getId(), INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.request.amount.positive")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnBadRequest_whenDescriptionIsNull() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT), null,
                category.getId(), INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.request.description.not.blank")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnBadRequest_whenDescriptionIsEmpty() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                "", category.getId(), INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.request.description.not.blank")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnBadRequest_whenCategoryIdIsNull() throws Exception {
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                DESCRIPTION, null, INTERVAL_DAYS);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.request.category.id.not.null")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void create_shouldReturnBadRequest_whenIntervalDaysLessThanOne() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransactionRequestDto dto = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                DESCRIPTION, category.getId(), 0);
        mockMvc.perform(post(API_RECURRING_TRANSACTION_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("1")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION_CREATE));
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void toggleActive_shouldSwitchActive_whenIdValid() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransaction recurringTransaction = new RecurringTransaction(new BigDecimal(AMOUNT), DESCRIPTION,
                category, user, INTERVAL_DAYS, LocalDate.now().minusDays(1));
        RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);
        mockMvc.perform(patch(API_RECURRING_TRANSACTION + "/" + saved.getId() + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.controller.toggle.active")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION +
                        "/" + saved.getId() + "/toggle"));
        assertThat(recurringTransactionRepository.findById(saved.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void toggleActive_shouldSwitchBackToActive_whenPreviouslyInactive() throws Exception {
        Category category = categoryRepository.save(new Category(CATEGORY_NAME));
        RecurringTransaction recurringTransaction = new RecurringTransaction(new BigDecimal(AMOUNT), DESCRIPTION,
                category, user, INTERVAL_DAYS, LocalDate.now().minusDays(1));
        recurringTransaction.setActive(false);
        RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);
        mockMvc.perform(patch(API_RECURRING_TRANSACTION + "/" + saved.getId() + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(msg("recurring.transaction.controller.toggle.active")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION +
                        "/" + saved.getId() + "/toggle"));
        assertThat(recurringTransactionRepository.findById(saved.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void toggleActive_shouldReturnBadRequest_whenIdInvalid() throws Exception {
        mockMvc.perform(patch(API_RECURRING_TRANSACTION + "/" + ID_INVALID + "/toggle"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(msg("handle.illegal.argument")))
                .andExpect(jsonPath("$.path").value(API_RECURRING_TRANSACTION +
                        "/" + ID_INVALID + "/toggle"));
    }
}
