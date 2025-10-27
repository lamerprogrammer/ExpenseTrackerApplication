package test.model;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.util.TestUtils;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;
import static test.util.TestMessageSource.msg;

public class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        User user = new User(ID_VALID, USER_EMAIL, USER_PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        User user = new User(ID_VALID, "not-email", USER_PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "email", msg("user.email.invalid")); 
    }

    @Test
    void shouldFail_whenEmailIsNull() {
        User user = new User(ID_VALID, null, USER_PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "email", msg("user.email.not-blank"));
    }

    @Test
    void shouldFail_whenEmailIsEmpty() {
        User user = new User(ID_VALID, "", USER_PASSWORD, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "email", msg("user.email.not-blank"));
    }

    @Test
    void shouldFail_whenPasswordIsNull() {
        User user = new User(ID_VALID, USER_EMAIL, null, Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "password", msg("user.password.not-blank"));
    }

    @Test
    void shouldFail_whenPasswordIsEmpty() {
        User user = new User(ID_VALID, USER_EMAIL, "", Set.of(Role.USER), true);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        TestUtils.assertHasViolation(violations, "password", msg("user.password.not-blank"));
    }

    @Test
    void shouldInitializeRolesAsEmptySetByDefault() {
        User user = new User();

        assertThat(user.getRoles()).isNotNull().isEmpty();
    }

    @Test
    void setters_shouldSetAllFieldsCorrectly() {
        BigDecimal amount = new BigDecimal("0");
        User user = new User();

        user.setId(ID_VALID);
        user.setEmail(MODERATOR_EMAIL);
        user.setPassword(USER_PASSWORD);
        user.setRoles(Set.of(Role.MODERATOR));
        user.setBanned(false);
        user.setTotalExpenses(amount);

        assertThat(user.getId()).isEqualTo(ID_VALID);
        assertThat(user.getEmail()).isEqualTo(MODERATOR_EMAIL);
        assertThat(user.getPassword()).isEqualTo(USER_PASSWORD);
        assertThat(user.getRoles().iterator().next()).isEqualTo(Role.MODERATOR);
        assertThat(user.isBanned()).isFalse();
        assertThat(user.getTotalExpenses()).isEqualByComparingTo(amount);
    }

    @Test
    void shouldBuildUserUsingBuilder() {
        User user = User.builder()
                .id(ID_VALID)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .roles(Set.of(Role.USER))
                .banned(true)
                .build();

        assertThat(user.getId()).isEqualTo(ID_VALID);
        assertThat(user.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(user.getPassword()).isEqualTo(USER_PASSWORD);
        assertThat(user.getRoles()).containsExactly(Role.USER);
        assertThat(user.isBanned()).isTrue();
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(User.class)
                .usingGetClass()
                .suppress(Warning.SURROGATE_KEY, Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .verify();
    }
}
