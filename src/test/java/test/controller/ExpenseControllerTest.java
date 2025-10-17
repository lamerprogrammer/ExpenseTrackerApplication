package test.controller;

import com.example.expensetracker.controller.ExpenseController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.DateRangeDto;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import test.util.TestData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserService userService;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    private ExpenseController expenseController;

    @Test
    void report_shouldReturnExpensesForPeriod() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        Instant from = Instant.now();
        Instant to = Instant.now();
        DateRangeDto range = new DateRangeDto();
        range.setFrom(from);
        range.setTo(to);
        ExpensesReportDto dto = new ExpensesReportDto(new BigDecimal(1000), List.of());
        when(expenseService.getReport(eq(details), eq(from), eq(to))).thenReturn(dto);

        var result = expenseController.report(range, details, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().total()).isEqualByComparingTo("1000");
    }

    @Test
    void getTotal_shouldReturnTotalExpenses() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        when(userService.getTotalExpenses(any())).thenReturn(new BigDecimal(1000));

        var result = expenseController.getTotal(details, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).isEqualByComparingTo("1000");
    }
}
