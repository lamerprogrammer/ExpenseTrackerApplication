package test.dto;

import com.example.expensetracker.dto.RegisterDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.TestData;
import test.util.UtilForTests;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;

public class RegisterDtoTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        RegisterDto registerDto = new RegisterDto(USER_NAME, USER_EMAIL, PASSWORD);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenNameIsNull() {
        RegisterDto registerDto = new RegisterDto(null, USER_EMAIL, PASSWORD);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "name", 
                "Имя пользователя не может быть пустым");
    }

    @Test
    void shouldFail_whenNameIsEmpty() {
        RegisterDto registerDto = new RegisterDto("", USER_EMAIL, PASSWORD);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "name",
                "Имя пользователя не может быть пустым");
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        RegisterDto registerDto = new RegisterDto(USER_NAME, "not-email", PASSWORD);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "email", "Некорректная почта");
    }

    @Test
    void shouldFail_whenEmailIsNull() {
        RegisterDto registerDto = new RegisterDto(USER_NAME, null, PASSWORD);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "email", "Почта обязательна");
    }

    @Test
    void shouldFail_whenEmailIsEmpty() {
        RegisterDto registerDto = new RegisterDto(USER_NAME, "", PASSWORD);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "email", "Почта обязательна");
    }

    @Test
    void shouldFail_whenPasswordIsNull() {
        RegisterDto registerDto = new RegisterDto(USER_NAME, USER_EMAIL, null);
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "password", "Пароль обязателен");
    }

    @Test
    void shouldFail_whenPasswordIsEmpty() {
        RegisterDto registerDto = new RegisterDto(USER_NAME, USER_EMAIL, "");
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "password", "Пароль обязателен");
    }
    
    @Test
    void setEmail_shouldUpdateField() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail(USER_EMAIL);
        
        assertThat(dto.getEmail()).isEqualTo(USER_EMAIL);
    }

    @Test
    void setPassword_shouldUpdateField() {
        RegisterDto dto = new RegisterDto();
        dto.setPassword(PASSWORD);

        assertThat(dto.getPassword()).isEqualTo(PASSWORD);
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(RegisterDto.class)
                .withOnlyTheseFields("name", "email", "password")
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }

    @Test
    void toStringTest() {
        RegisterDto dto = TestData.registerDto();
        assertThat(dto).asString().contains(USER_NAME).contains(USER_EMAIL).contains("****");
    }
}
