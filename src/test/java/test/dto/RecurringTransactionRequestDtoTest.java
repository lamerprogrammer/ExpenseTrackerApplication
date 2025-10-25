package test.dto;

import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.TestUtils;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;
import static test.util.TestMessageSource.msg;

public class RecurringTransactionRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT), 
                DESCRIPTION, ID_CATEGORY, INTERVAL_DAYS);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenAmountIsNull() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(null,
                DESCRIPTION, ID_CATEGORY, INTERVAL_DAYS);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "amount", msg("recurring.transaction.request.dto.amount.not.null"));
    }

    @Test
    void shouldFail_whenAmountIsNegative() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT_NEGATIVE),
                DESCRIPTION, ID_CATEGORY, INTERVAL_DAYS);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "amount", msg("recurring.transaction.request.dto.amount.positive"));
    }

    @Test
    void shouldFail_whenDescriptionIsNull() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                null, ID_CATEGORY, INTERVAL_DAYS);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "description", msg("recurring.transaction.request.dto.description"));
    }

    @Test
    void shouldFail_whenDescriptionIsEmpty() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                "", ID_CATEGORY, INTERVAL_DAYS);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "description", msg("recurring.transaction.request.dto.description"));
    }

    @Test
    void shouldFail_whenCategoryIdIsNull() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                DESCRIPTION, null, INTERVAL_DAYS);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "categoryId", msg("recurring.transaction.request.dto.categoryId"));
    }

    @Test
    void shouldFail_whenIntervalDaysIsZero() {
        RecurringTransactionRequestDto request = new RecurringTransactionRequestDto(new BigDecimal(AMOUNT),
                DESCRIPTION, ID_CATEGORY, 0);
        Set<ConstraintViolation<RecurringTransactionRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "intervalDays", "должно быть не меньше 1");
    }
}
