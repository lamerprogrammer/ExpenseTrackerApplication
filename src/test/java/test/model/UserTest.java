package test.model;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.UtilForTests;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;

public class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        User user = new User(42L, USER_EMAIL, PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        User user = new User(42L, "not-email", PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "email", "Некорректная почта");
    }

    @Test
    void shouldFail_whenEmailIsNull() {
        User user = new User(42L, null, PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "email",
                "Почта обязательна");
    }

    @Test
    void shouldFail_whenEmailIsEmpty() {
        User user = new User(42L, "", PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "email",
                "Почта обязательна");
    }

    @Test
    void shouldFail_whenPasswordIsNull() {
        User user = new User(42L, USER_EMAIL, null, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "password",
                "Пароль обязателен");
    }

    @Test
    void shouldFail_whenPasswordIsEmpty() {
        User user = new User(42L, USER_EMAIL, "", Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        UtilForTests.assertHasViolation(violations, "password",
                "Пароль обязателен");
    }

    @Test
    void shouldInitializeRolesAsEmptySetByDefault() {
        User user = new User();

        assertThat(user.getRoles()).isNotNull().isEmpty();
    }

    @Test
    void setters_shouldSetAllFieldsCorrectly() {
        User user = new User();

        user.setId(42L);
        user.setEmail(MODERATOR_EMAIL);
        user.setPassword(PASSWORD);
        user.setRoles(Set.of(Role.MODERATOR));
        user.setBanned(false);

        assertThat(user.getId()).isEqualTo(42L);
        assertThat(user.getEmail()).isEqualTo(MODERATOR_EMAIL);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
        assertThat(user.getRoles().iterator().next()).isEqualTo(Role.MODERATOR);
        assertThat(user.isBanned()).isFalse();
    }

    @Test
    void shouldBuildUserUsingBuilder() {
        User user = User.builder()
                .id(42L)
                .email(USER_EMAIL)
                .password(PASSWORD)
                .roles(Set.of(Role.USER))
                .banned(true)
                .build();

        assertThat(user.getId()).isEqualTo(42L);
        assertThat(user.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
        assertThat(user.getRoles()).containsExactly(Role.USER);
        assertThat(user.isBanned()).isTrue();
    }
}
