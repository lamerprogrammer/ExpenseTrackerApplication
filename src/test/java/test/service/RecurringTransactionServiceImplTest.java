package test.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.mapper.RecurringTransactionMapper;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.RecurringTransaction;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.RecurringTransactionRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.RecurringTransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import test.util.TestData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class RecurringTransactionServiceImplTest {

    @Mock
    private RecurringTransactionRepository recurringRepo;

    @Mock
    private ExpenseRepository expenseRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RecurringTransactionMapper mapper;

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringTransactionServiceImpl;

    @Test
    void processRecurringTransactions_shouldCreateNewTransactionAndUpdateDate() {
        RecurringTransaction recurring = TestData.recurringTransaction();
        List<RecurringTransaction> list = List.of(recurring);
        when(recurringRepo.findAllByNextExecutionDateLessThanEqual(any(LocalDate.class))).thenReturn(list);

        recurringTransactionServiceImpl.processRecurringTransactions();

        assertThat(recurring.getNextExecutionDate()).isAfter(LocalDate.now());
        assertThat(recurring.getAmount()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(recurringRepo).findAllByNextExecutionDateLessThanEqual(any());
        verify(expenseRepo).save(any(Expense.class));
        verify(recurringRepo).save(any(RecurringTransaction.class));
    }

    @Test
    void getUserRecurringTransactions_shouldReturnTransactions_whenUserExists() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        RecurringTransaction recurring = TestData.recurringTransaction();
        RecurringTransactionDto dto = TestData.recurringTransactionDtoActiveTrue();
        List<RecurringTransaction> list = List.of(recurring);
        when(userRepo.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(recurringRepo.findAllByUser_Email(user.getEmail())).thenReturn(list);
        when(mapper.toDto(recurring)).thenReturn(dto);

        var result = recurringTransactionServiceImpl.getUserRecurringTransactions(currentUser);

        assertThat(result.get(0)).extracting(RecurringTransactionDto::getId, RecurringTransactionDto::getIntervalDays)
                .containsExactly(ID_TRANSACTION, INTERVAL_DAYS);
        assertThat(recurring.getAmount()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(userRepo).findByEmail(currentUser.getUsername());
        verify(recurringRepo).findAllByUser_Email(user.getEmail());
        verify(mapper).toDto(recurring);
    }

    @Test
    void getUserRecurringTransactions_shouldThrowException_whenUserNotFound() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        when(userRepo.findByEmail(currentUser.getUsername())).thenReturn(Optional.empty());

        UserNotFoundByIdException ex = assertThrows(UserNotFoundByIdException.class,
                () -> recurringTransactionServiceImpl.getUserRecurringTransactions(currentUser));

        assertThat(ex.getMessage()).isEqualTo("User not found");
        verify(userRepo).findByEmail(currentUser.getUsername());
        verify(recurringRepo, never()).findAllByUser_Email(anyString());
        verify(mapper, never()).toDto(any());
    }

    @Test
    void createRecurringTransaction_shouldCreateAndSaveTransactions_whenUserAndCategoryExist() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        RecurringTransaction recurring = TestData.recurringTransaction();
        Category category = recurring.getCategory();
        RecurringTransactionRequestDto requestDto = TestData.recurringTransactionRequestDto();
        RecurringTransactionDto dto = TestData.recurringTransactionDtoActiveTrue();
        RecurringTransaction entity = TestData.recurringTransaction();
        when(userRepo.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(categoryRepo.findById(requestDto.categoryId())).thenReturn(Optional.of(category));
        when(mapper.fromRequest(requestDto, category)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        when(recurringRepo.save(entity)).thenAnswer(invocation -> invocation.getArgument(0));

        var result = recurringTransactionServiceImpl.createRecurringTransaction(currentUser, requestDto);

        assertThat(result).extracting(RecurringTransactionDto::getId, RecurringTransactionDto::getIntervalDays)
                .containsExactly(ID_TRANSACTION, INTERVAL_DAYS);
        assertThat(recurring.getAmount()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(userRepo).findByEmail(currentUser.getUsername());
        verify(categoryRepo).findById(requestDto.categoryId());
        verify(mapper).toDto(entity);
    }

    @Test
    void createRecurringTransaction_shouldThrowException_whenUserNotFound() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        RecurringTransactionRequestDto requestDto = TestData.recurringTransactionRequestDto();
        when(userRepo.findByEmail(currentUser.getUsername())).thenReturn(Optional.empty());

        UserNotFoundByIdException ex = assertThrows(UserNotFoundByIdException.class,
                () -> recurringTransactionServiceImpl.createRecurringTransaction(currentUser, requestDto));

        assertThat(ex.getMessage()).isEqualTo("User not found");
        verify(userRepo).findByEmail(currentUser.getUsername());
        verify(categoryRepo, never()).findById(any());
        verify(mapper, never()).toDto(any());
    }

    @Test
    void createRecurringTransaction_shouldThrowException_whenCategoryNotFound() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        RecurringTransactionRequestDto requestDto = TestData.recurringTransactionRequestDto();
        when(userRepo.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(categoryRepo.findById(requestDto.categoryId())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recurringTransactionServiceImpl.createRecurringTransaction(currentUser, requestDto));

        assertThat(ex.getMessage()).isEqualTo("Category not found");
        verify(userRepo).findByEmail(currentUser.getUsername());
        verify(categoryRepo).findById(requestDto.categoryId());
        verify(mapper, never()).toDto(any());
    }

    @Test
    void toggleActive_shouldSwitchActive_whenTransactionExistsAndTrue() {
        RecurringTransactionDto dto = TestData.recurringTransactionDtoActiveFalse();
        RecurringTransaction entity = TestData.recurringTransaction();
        Long id = entity.getId();
        when(recurringRepo.findById(id)).thenReturn(Optional.of(entity));
        when(recurringRepo.save(entity)).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(entity)).thenReturn(dto);

        var result = recurringTransactionServiceImpl.toggleActive(id);

        assertThat(result.isActive()).isFalse();
        verify(recurringRepo).findById(id);
        verify(recurringRepo).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void toggleActive_shouldSwitchActive_whenTransactionExistsAndFalse() {
        RecurringTransactionDto dto = TestData.recurringTransactionDtoActiveTrue();
        RecurringTransaction entity = TestData.recurringTransaction();
        Long id = entity.getId();
        when(recurringRepo.findById(id)).thenReturn(Optional.of(entity));
        when(recurringRepo.save(entity)).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(entity)).thenReturn(dto);

        var result = recurringTransactionServiceImpl.toggleActive(id);

        assertThat(result.isActive()).isTrue();
        verify(recurringRepo).findById(id);
        verify(recurringRepo).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void toggleActive_shouldReturnException_whenTransactionNotFound() {
        RecurringTransaction entity = TestData.recurringTransaction();
        Long id = entity.getId();
        when(recurringRepo.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recurringTransactionServiceImpl.toggleActive(id));

        assertThat(ex.getMessage()).isEqualTo("Recurring transaction not found");
        verify(recurringRepo).findById(id);
        verify(recurringRepo, never()).save(any());
        verify(mapper, never()).toDto(any());
    }
}

