package test.dto;

import com.example.expensetracker.dto.ChangePasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.TestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.USER_PASSWORD;
import static test.util.Constants.USER_PASSWORD_NEW;
import static test.util.TestMessageSource.msg;

public class ChangePasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        ChangePasswordRequest request = new ChangePasswordRequest(USER_PASSWORD, USER_PASSWORD_NEW);
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenPasswordIsNull() {
        ChangePasswordRequest request = new ChangePasswordRequest(null, USER_PASSWORD_NEW);
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "oldPassword", msg("user.password.old.not-blank"));
    }

    @Test
    void shouldFail_whenPasswordIsEmpty() {
        ChangePasswordRequest request = new ChangePasswordRequest("", USER_PASSWORD_NEW);
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "oldPassword", msg("user.password.old.not-blank"));
    }

    @Test
    void shouldFail_whenNewPasswordIsNull() {
        ChangePasswordRequest request = new ChangePasswordRequest(USER_PASSWORD, null);
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "newPassword", msg("user.password.new.not-blank"));
    }

    @Test
    void shouldFail_whenNewPasswordIsEmpty() {
        ChangePasswordRequest request = new ChangePasswordRequest(USER_PASSWORD, "");
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "newPassword", msg("user.password.new.not-blank"));
    }
}
