package test.controller;

import com.example.expensetracker.controller.RecurringTransactionController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.RecurringTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import test.util.TestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class RecurringTransactionControllerTest {

    @Mock
    private RecurringTransactionService service;

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    RecurringTransactionController controller;

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }

    @Test
    void getAll_shouldReturnAllTransactions() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        RecurringTransactionDto transactionDto = TestData.recurringTransactionDtoActiveTrue();
        List<RecurringTransactionDto> dtoList = List.of(transactionDto);
        when(service.getUserRecurringTransactions(currentUser)).thenReturn(dtoList);

        var result = controller.getAll(currentUser, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().get(0)).extracting(
                        RecurringTransactionDto::getId,
                        RecurringTransactionDto::getCategoryId,
                        RecurringTransactionDto::getIntervalDays,
                        RecurringTransactionDto::getDescription,
                        RecurringTransactionDto::getCategoryName)
                .containsExactly(
                        ID_TRANSACTION,
                        ID_CATEGORY,
                        INTERVAL_DAYS,
                        DESCRIPTION,
                        CATEGORY_NAME
                );
        verify(service).getUserRecurringTransactions(currentUser);
        verify(messageSource).getMessage(eq("recurring.transaction.controller.get.all"), isNull(), any());
    }

    @Test
    void create_shouldReturnNewTransaction() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        RecurringTransactionRequestDto dto = TestData.recurringTransactionRequestDto();
        RecurringTransactionDto created = TestData.recurringTransactionDtoActiveTrue();
        when(service.createRecurringTransaction(currentUser, dto)).thenReturn(created);

        var result = controller.create(currentUser, dto, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).extracting(
                        RecurringTransactionDto::getId,
                        RecurringTransactionDto::getCategoryId,
                        RecurringTransactionDto::getIntervalDays,
                        RecurringTransactionDto::getDescription,
                        RecurringTransactionDto::getCategoryName)
                .containsExactly(
                        ID_TRANSACTION,
                        ID_CATEGORY,
                        INTERVAL_DAYS,
                        DESCRIPTION,
                        CATEGORY_NAME
                );
        verify(service).createRecurringTransaction(currentUser, dto);
        verify(messageSource).getMessage(eq("recurring.transaction.controller.create"), isNull(), any());
    }

    @Test
    void toggleActive_shouldReturnAllTransactions() {
        RecurringTransactionDto dto = TestData.recurringTransactionDtoActiveFalse();
        when(service.toggleActive(ID_TRANSACTION)).thenReturn(dto);

        var result = controller.toggleActive(ID_TRANSACTION, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).extracting(
                        RecurringTransactionDto::getId,
                        RecurringTransactionDto::getCategoryId,
                        RecurringTransactionDto::getIntervalDays,
                        RecurringTransactionDto::getDescription,
                        RecurringTransactionDto::getCategoryName,
                        RecurringTransactionDto::isActive)
                .containsExactly(
                        ID_TRANSACTION,
                        ID_CATEGORY,
                        INTERVAL_DAYS,
                        DESCRIPTION,
                        CATEGORY_NAME,
                        false
                );
        verify(service).toggleActive(ID_TRANSACTION);
        verify(messageSource).getMessage(eq("recurring.transaction.controller.toggle.active"), isNull(), any());
    }
}

