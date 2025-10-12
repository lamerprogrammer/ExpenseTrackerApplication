package test.dto;

import com.example.expensetracker.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static com.example.expensetracker.dto.ApiResponseFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;
import static test.util.Constants.API_TEST_ENDPOINT;

@ExtendWith(MockitoExtension.class)
public class ApiResponseFactoryTest {

    @Mock
    HttpServletRequest request;

    private final String path = API_TEST_ENDPOINT;

    @Test
    void success_shouldReturn200() {
        String data = "ok";
        String message = "успех";
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);

        var result = success(data, message, request);

        assertThat(result.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(result.getMessage()).isEqualTo(message);
        assertThat(result.getPath()).isEqualTo(path);
        assertThat(result.getData()).isEqualTo(data);
    }

    @Test
    void error_shouldReturn500() {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String error = "500";
        String message = "ошибка сервера";
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);

        var result = error(status, error, message, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(body.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body.getError()).isEqualTo(error);
        assertThat(body.getMessage()).isEqualTo(message);
        assertThat(body.getPath()).isEqualTo(path);
    }

    @Test
    void validationError_shouldReturn400() {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "ошибка клиента";
        List<String> errors = List.of("Exception");
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);

        var result = validationError(status, message, request, errors);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = result.getBody();
        assertThat(body).isNotNull();
        Objects.requireNonNull(body);
        assertThat(body.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(body.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body.getError()).isEqualTo("ValidationError");
        assertThat(body.getMessage()).isEqualTo(message);
        assertThat(body.getPath()).isEqualTo(path);
        List<String> er = body.getErrors();
        assertThat(er).containsExactly("Exception");
    }

    @Test
    void unauthorized_shouldReturnResponse401() {
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);

        var result = unauthorized(request);

        assertThat(result.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getError()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        assertThat(result.getMessage()).isEqualTo("Доступ запрещён: требуется авторизация");
        assertThat(result.getPath()).isEqualTo(path);
    }

    @Test
    void forbidden_shouldReturnResponse403() {
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);

        var result = forbidden(request);

        assertThat(result.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getError()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(result.getMessage()).isEqualTo("У вас недостаточно прав для доступа к этому ресурсу");
        assertThat(result.getPath()).isEqualTo(path);
    }
}

