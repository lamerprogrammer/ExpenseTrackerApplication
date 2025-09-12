package test.util;

import com.example.expensetracker.dto.LoginDto;
import jakarta.validation.ConstraintViolation;
import org.springframework.mock.web.DelegatingServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class UtilForTests {

    private UtilForTests() {
    }

    public static DelegatingServletOutputStream writeByteToStream(ByteArrayOutputStream outputStream) {
        return new DelegatingServletOutputStream(outputStream);
    }

    public static <T> void assertHasViolation(Set<ConstraintViolation<T>> violations,
                                              String field,
                                              String expectedMessageFragment) {
        assertThat(violations)
                .as("Ожидалась ошибка на поле '%s', содержащая '%s'", field, expectedMessageFragment)
                .anySatisfy(v -> {
                    assertThat(v.getPropertyPath().toString()).isEqualTo(field);
                    assertThat(v.getMessage()).containsIgnoringCase(expectedMessageFragment);
                });
        
        boolean found = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field)
                        && v.getMessage().toLowerCase().contains(expectedMessageFragment.toLowerCase()));
        assertTrue(found, () -> String.format(
                "Ожидалась ошика на поле '%s', содержащая '%s'. Но имеем: '%s'",
                field, expectedMessageFragment, violations));
    }
}