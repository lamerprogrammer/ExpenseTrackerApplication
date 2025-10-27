package test.model;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.RecurringTransaction;
import com.example.expensetracker.model.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;

public class RecurringTransactionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void constructor_shouldSuccess_whenAllFieldsValid() {
        BigDecimal amount = new BigDecimal(AMOUNT);
        Category category = new Category();
        User user = new User();
        LocalDate executionDate = LocalDate.now().minusDays(1);
        RecurringTransaction transaction = new RecurringTransaction(amount, DESCRIPTION, category,
                user, INTERVAL_DAYS, executionDate);

        assertThat(transaction).extracting(
                        RecurringTransaction::getAmount,
                        RecurringTransaction::getDescription,
                        RecurringTransaction::getCategory,
                        RecurringTransaction::getUser,
                        RecurringTransaction::getIntervalDays,
                        RecurringTransaction::getNextExecutionDate)
                .containsExactly(
                        amount,
                        DESCRIPTION,
                        category,
                        user,
                        INTERVAL_DAYS,
                        executionDate
                );
    }

    @Test
    void setters_shouldSuccess_whenAllFieldsValid() {
        BigDecimal amount = new BigDecimal(AMOUNT);
        Category category = new Category();
        User user = new User();
        LocalDate executionDate = LocalDate.now().minusDays(1);
        RecurringTransaction transaction = new RecurringTransaction();
        transaction.setAmount(amount);
        transaction.setDescription(DESCRIPTION);
        transaction.setCategory(category);
        transaction.setUser(user);
        transaction.setIntervalDays(INTERVAL_DAYS);
        transaction.setNextExecutionDate(executionDate);

        assertThat(transaction).extracting(
                        RecurringTransaction::getAmount,
                        RecurringTransaction::getDescription,
                        RecurringTransaction::getCategory,
                        RecurringTransaction::getUser,
                        RecurringTransaction::getIntervalDays,
                        RecurringTransaction::getNextExecutionDate)
                .containsExactly(
                        amount,
                        DESCRIPTION,
                        category,
                        user,
                        INTERVAL_DAYS,
                        executionDate
                );
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(RecurringTransaction.class)
                .usingGetClass()
                .suppress(Warning.SURROGATE_KEY, Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .verify();
    }
}
