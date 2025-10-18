package test.dto;

import com.example.expensetracker.dto.DateRangeDto;
import com.example.expensetracker.dto.LoginDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Test
    void shouldFail_whenFromIsNull() {
        DateRangeDto dto = getDateRangeDto(null, 2000);
        
        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);//вот тут два нарушения, но я не знаю как проверить message
    }

    @Test
    void shouldFail_whenToIsNull() {
        DateRangeDto dto = getDateRangeDto(1000, null);
        
        Set<ConstraintViolation<DateRangeDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);//вот тут два нарушения, но я не знаю как проверить message
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
