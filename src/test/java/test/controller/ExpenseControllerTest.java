package test.controller;

import com.example.expensetracker.controller.ExpenseController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.DateRangeDto;
import com.example.expensetracker.dto.ExpensesReportDto;
import com.example.expensetracker.model.Month;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.AMOUNT;

@ExtendWith(MockitoExtension.class)
public class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ExpenseController expenseController;

    @Test
    void report_shouldReturnExpensesForPeriod() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Instant from = Instant.now();
        Instant to = Instant.now();
        DateRangeDto range = new DateRangeDto();
        range.setFrom(from);
        range.setTo(to);
        ExpensesReportDto dto = new ExpensesReportDto(new BigDecimal(AMOUNT), List.of());
        when(expenseService.getReport(currentUser, from, to)).thenReturn(dto);
        mockMessage();
        
        var result = expenseController.report(range, currentUser, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().total()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(expenseService).getReport(currentUser, from, to);
        verify(messageSource).getMessage(eq("expense.controller.report.ok"), any(), any());
    }

    @Test
    void reportMonthly_shouldReturnExpensesForPeriod() {
        Month september = Month.SEPTEMBER;
        Integer year = 2025;
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        ExpensesReportDto dto = new ExpensesReportDto(new BigDecimal(AMOUNT), List.of());
        when(expenseService.getReportMonthly(september, year, currentUser)).thenReturn(dto);
        mockMessage();

        var result = expenseController.reportMonthly(september, year, currentUser, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().total()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(expenseService).getReportMonthly(september, year, currentUser);
        verify(messageSource).getMessage(eq("expense.controller.report.monthly"), any(), any());
    }

    @Test
    void getTotal_shouldReturnTotalExpenses() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        when(userService.getTotalExpenses(anyLong())).thenReturn(new BigDecimal(AMOUNT));
        mockMessage();

        var result = expenseController.getTotal(details, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(userService).getTotalExpenses(anyLong());
        verify(messageSource).getMessage(eq("expense.controller.total.ok"), any(), any());
    }

    private void mockMessage() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }
}
