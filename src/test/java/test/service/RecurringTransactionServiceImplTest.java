package test.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.RecurringTransaction;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.RecurringTransactionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.AMOUNT;

@ExtendWith(MockitoExtension.class)
public class RecurringTransactionServiceImplTest {
    
    @Mock
    private RecurringTransactionRepository recurringRepo;

    @Mock
    private ExpenseRepository expenseRepo;
    
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
}
