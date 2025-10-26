package test.dto;

import com.example.expensetracker.dto.DateRangeDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.TestUtils;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.TestMessageSource.msg;

public class DateRangeDtoTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void shouldPass_whenAllFieldsValid() {
        DateRangeDto dto = getDateRangeDto(1000, 2000);

        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenFromAfterTo() {
        DateRangeDto dto = getDateRangeDto(3000, 2000);
        
        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "validRange", msg("date.range.dto.from.is.valid.range"));
    }

    @Test
    void shouldFail_whenFromIsNull() {
        DateRangeDto dto = getDateRangeDto(null, 2000);
        
        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
        TestUtils.assertHasViolation(violations, "from", msg("date.range.dto.from.not.null"));
        TestUtils.assertHasViolation(violations, "validRange", msg("date.range.dto.from.is.valid.range"));
    }

    @Test
    void shouldFail_whenToIsNull() {
        DateRangeDto dto = getDateRangeDto(1000, null);
        
        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
        TestUtils.assertHasViolation(violations, "to", msg("date.range.dto.to.not.null"));
        TestUtils.assertHasViolation(violations, "validRange", msg("date.range.dto.from.is.valid.range"));
    }

    @Test
    void isValidRange_shouldReturnTrue_whenAllFieldsValid() {
        DateRangeDto dto = getDateRangeDto(1000, 2000);

        boolean result = dto.isValidRange();

        assertThat(result).isTrue();
    }

    private static DateRangeDto getDateRangeDto(Integer from, Integer to) {
        DateRangeDto dto = new DateRangeDto();
        if (from != null) dto.setFrom(Instant.ofEpochSecond(from));
        if (to != null) dto.setTo(Instant.ofEpochSecond(to));
        return dto;
    }
}
