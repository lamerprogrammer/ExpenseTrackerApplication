package test.exception;

import com.example.expensetracker.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class UnauthorizedExceptionTest {
    @Test
    public void shouldStoreMessage() {
        String message = "User is not authorized";
        UnauthorizedException ex = new UnauthorizedException(message);

        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void shouldBeAnnotatedWithUnauthorizedStatus() {
        ResponseStatus annotation = UnauthorizedException.class.getAnnotation(ResponseStatus.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
