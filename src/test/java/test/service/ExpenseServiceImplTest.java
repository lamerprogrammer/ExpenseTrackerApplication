package test.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.CategorySumDto;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Month;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.ExpenseServiceImpl;
import com.example.expensetracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import test.util.TestData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static test.util.Constants.AMOUNT;
import static test.util.Constants.CATEGORY_NAME;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    void getReport_shouldReturnExpensesForPeriod() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Instant from = Instant.now();
        Instant to = Instant.now();
        when(expenseRepository.sumByCategoryForUserBetween(user.getId(), from, to)).thenReturn(List.of(
                new CategorySumDto(CATEGORY_NAME, new BigDecimal("500")),
                new CategorySumDto("drink", new BigDecimal("300"))));
        when(expenseRepository.totalForUserBetween(user.getId(), from, to)).thenReturn(new BigDecimal("800"));

        var result = expenseService.getReport(currentUser, from, to);

        assertThat(result.total()).isEqualByComparingTo("800");
        assertThat(result.byCategory()).hasSize(2);
        verify(expenseRepository).sumByCategoryForUserBetween(eq(user.getId()), eq(from), eq(to));
        verify(expenseRepository).totalForUserBetween(eq(user.getId()), eq(from), eq(to));
    }

    @Test
    void addExpense_shouldIncreaseTotalExpenses_whenUserExists() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Expense expense = TestData.expense();
        when(userRepository.findById(currentUser.getDomainUser().getId())).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        var result = expenseService.addExpense(currentUser, expense);

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        verify(userService).clearTotalExpensesCache(anyLong());
    }

    @Test
    void addExpense_shouldThrowException_whenUserNotFound() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Expense expense = TestData.expense();
        when(userRepository.findById(currentUser.getDomainUser().getId())).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> expenseService.addExpense(currentUser, expense));

        assertThat(ex.getMessage()).isNotBlank();
        verify(expenseRepository, never()).save(any());
        verify(userService, never()).clearTotalExpensesCache(anyLong());
    }

    @Test
    void deleteExpense_shouldDeleteExpense_whenUserExists() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Expense expense = TestData.expense();
        expense.setUser(user);
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(currentUser, expense.getId());

        verify(userRepository).save(any(User.class));
        verify(expenseRepository).delete(any(Expense.class));
        verify(userService).clearTotalExpensesCache(anyLong());
    }

    @Test
    void deleteExpense_shouldThrowException_whenExpenseNotFound() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Expense expense = TestData.expense();
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> expenseService.deleteExpense(currentUser, expense.getId()));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(expenseRepository, never()).delete(any(Expense.class));
        verify(userService, never()).clearTotalExpensesCache(anyLong());
    }

    @Test
    void deleteExpense_shouldThrowException_whenExpenseAlien() {
        User user = TestData.user();
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        Expense expense = TestData.expense();
        expense.setUser(admin);
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> expenseService.deleteExpense(currentUser, expense.getId()));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(expenseRepository, never()).delete(any(Expense.class));
        verify(userService, never()).clearTotalExpensesCache(anyLong());
    }

    @Test
    void getReportMonthly_shouldReturnExpensesForMonth_whenUserExists() {
        Month september = Month.SEPTEMBER;
        Integer year = 2025;
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        CategorySumDto categorySumDto = new CategorySumDto(CATEGORY_NAME, new BigDecimal(AMOUNT));
        List<CategorySumDto> items = List.of(categorySumDto);
        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(expenseRepository.getMonthlyReport(eq(user), any(), any())).thenReturn(items);
        
        var result = expenseService.getReportMonthly(september, year, currentUser);
        
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        assertThat(result.byCategory().get(0)).extracting(CategorySumDto::categoryName, CategorySumDto::sum)
                .containsExactly(categorySumDto.categoryName(), categorySumDto.sum());
        verify(userRepository).findByEmail(currentUser.getUsername());
        verify(expenseRepository).getMonthlyReport(eq(user), any(), any());
    }

    @Test
    void getReportMonthly_shouldReturnExpensesForMonth_whenUserNotFound() {
        Month september = Month.SEPTEMBER;
        Integer year = 2025;
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> expenseService.getReportMonthly(september, year, currentUser));

        assertThat(ex.getMessage()).isEqualTo("User not found");
        verify(userRepository).findByEmail(currentUser.getUsername());
        verify(expenseRepository, never()).getMonthlyReport(any(), any(), any());
    }

    @Test
    void getReportMonthly_shouldReturnExpensesForMonth_whenUserExistsAndYearIsNull() {
        Month september = Month.SEPTEMBER;
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        CategorySumDto categorySumDto = new CategorySumDto(CATEGORY_NAME, new BigDecimal(AMOUNT));
        List<CategorySumDto> items = List.of(categorySumDto);
        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(expenseRepository.getMonthlyReport(eq(user), any(), any())).thenReturn(items);

        var result = expenseService.getReportMonthly(september, null, currentUser);

        assertThat(result.total()).isEqualByComparingTo(new BigDecimal(AMOUNT));
        assertThat(result.byCategory().get(0)).extracting(CategorySumDto::categoryName, CategorySumDto::sum)
                .containsExactly(categorySumDto.categoryName(), categorySumDto.sum());
        verify(userRepository).findByEmail(currentUser.getUsername());
        verify(expenseRepository).getMonthlyReport(eq(user), any(), any());
    }
}
