package test.dto;

import com.example.expensetracker.dto.RecurringTransactionDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test.util.Constants.*;

public class RecurringTransactionDtoTest {

    @Test
    void shouldSetFieldsUsingConstructor() throws Exception {
        BigDecimal amount = new BigDecimal(AMOUNT);
        LocalDate now = LocalDate.now();
        RecurringTransactionDto result = new RecurringTransactionDto(ID_TRANSACTION, amount,
                DESCRIPTION, ID_CATEGORY, CATEGORY_NAME, INTERVAL_DAYS, now, true);

        assertThat(result).extracting(
                RecurringTransactionDto::getId,
                RecurringTransactionDto::getAmount,
                RecurringTransactionDto::getDescription,
                RecurringTransactionDto::getCategoryId,
                RecurringTransactionDto::getCategoryName,
                RecurringTransactionDto::getIntervalDays,
                RecurringTransactionDto::getNextExecutionDate)
                .containsExactly(
                        ID_TRANSACTION,
                        amount,
                        DESCRIPTION,
                        ID_CATEGORY,
                        CATEGORY_NAME,
                        INTERVAL_DAYS,
                        now
                );
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void constructor_shouldThrowException_whenAmountIsNull() throws Exception {
        var ex = checkEx(null, DESCRIPTION, ID_CATEGORY, LocalDate.now());
        
        assertThat(ex.getMessage()).isEqualTo("RecurringTransactionDto: mandatory field is null");
    }

    @Test
    void constructor_shouldThrowException_whenDescriptionIsNull() throws Exception {
        var ex = checkEx(new BigDecimal(AMOUNT), null, ID_CATEGORY, LocalDate.now());

        assertThat(ex.getMessage()).isEqualTo("RecurringTransactionDto: mandatory field is null");
    }

    @Test
    void constructor_shouldThrowException_whenCategoryIdIsNull() throws Exception {
        var ex = checkEx(new BigDecimal(AMOUNT), DESCRIPTION, null, LocalDate.now());

        assertThat(ex.getMessage()).isEqualTo("RecurringTransactionDto: mandatory field is null");
    }

    @Test
    void constructor_shouldThrowException_whenNextExecutionDateIsNull() throws Exception {
        var ex = checkEx(new BigDecimal(AMOUNT), DESCRIPTION, ID_CATEGORY, null);

        assertThat(ex.getMessage()).isEqualTo("RecurringTransactionDto: mandatory field is null");
    }
    
    private IllegalArgumentException checkEx(BigDecimal amount, String description, 
                                             Long categoryId, LocalDate nextExecutionDate) {
        return assertThrows(IllegalArgumentException.class,
                () -> new RecurringTransactionDto(ID_TRANSACTION, amount,
                        description, categoryId, CATEGORY_NAME, INTERVAL_DAYS, nextExecutionDate, true));
    } 
}
