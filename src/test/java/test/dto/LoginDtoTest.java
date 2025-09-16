package test.dto;

import com.example.expensetracker.dto.LoginDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.TestData;
import test.util.TestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;

public class LoginDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        LoginDto loginDto = new LoginDto(USER_EMAIL, PASSWORD);
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        LoginDto registerDto = new LoginDto("not-email", PASSWORD);
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "email", "Некорректная почта");
    }

    @Test
    void shouldFail_whenEmailIsNull() {
        LoginDto loginDto = new LoginDto(null, PASSWORD);
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "email", "Почта обязательна");
    }

    @Test
    void shouldFail_whenEmailIsEmpty() {
        LoginDto loginDto = new LoginDto("", PASSWORD);
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "email", "Почта обязательна");
    }

    @Test
    void shouldFail_whenPasswordIsNull() {
        LoginDto loginDto = new LoginDto(USER_EMAIL, null);
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "password", "Пароль обязателен");
    }

    @Test
    void shouldFail_whenPasswordIsEmpty() {
        LoginDto loginDto = new LoginDto(USER_EMAIL, "");
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(loginDto);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "password", "Пароль обязателен");
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(LoginDto.class)
                .withOnlyTheseFields("email", "password")
                .usingGetClass()
                .verify();
    }

    @Test
    void toStringTest() {
        LoginDto dto = TestData.loginDto();
        assertThat(dto).asString().contains(USER_EMAIL).contains("****");
    }
}
