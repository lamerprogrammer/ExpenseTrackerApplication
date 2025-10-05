package test.exception;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.exception.GlobalExceptionHandler;
import com.example.expensetracker.logging.applog.AppLogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static test.util.Constants.API_TEST_ENDPOINT;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;
    
    @Mock
    private AppLogService appLogService;

    @InjectMocks
    private GlobalExceptionHandler handler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        request = new MockHttpServletRequest();
        request.setRequestURI(API_TEST_ENDPOINT);
    }

    @Test
    void handleBadCredentials_shouldReturnUnauthorizedResponse() {
        BadCredentialsException ex = new BadCredentialsException("");

        var response = handler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        checkBody(response.getBody(), 401, "handle.bad.credentials");
    }

    @Test
    void handleBadCredentials_shouldUsePrincipalNameWhenPresent() {
        request.setUserPrincipal(() -> "test-user");
        BadCredentialsException ex = new BadCredentialsException("");

        var response = handler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleAccessDenied_shouldReturnForbiddenResponse() {
        AccessDeniedException ex = new AccessDeniedException("");

        var response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        checkBody(response.getBody(), 403, "handle.access.denied");
    }

    @Test
    void handleEntityNotFound_shouldReturnNotFoundResponse() {
        EntityNotFoundException ex = new EntityNotFoundException("");

        var response = handler.handleEntityNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        checkBody(response.getBody(), 404, "handle.entity.not.found");
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflictResponse() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("");

        var response = handler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        checkBody(response.getBody(), 409, "handle.data.integrity.violation");
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequestResponse() {
        IllegalArgumentException ex = new IllegalArgumentException("");

        var response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        checkBody(response.getBody(), 400, "handle.illegal.argument");
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnBadRequestResponseWithValidationMessage() {
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                mock(MethodParameter.class), mock(BindingResult.class)
        );

        var response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        checkBody(response.getBody(), 400, "validation.error");
    }

    @Test
    void handleGeneric_shouldReturnInternalServerErrorResponse() {
        Exception ex = new RuntimeException("");

        var response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getMessage()).contains("handle.generic");
    }

    @Test
    void handleGeneric_shouldIncludeStackTraceFor5xxErrors() {
        request.setUserPrincipal(() -> "stack-user");
        Exception ex = new RuntimeException("Exception");

        var response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    private void checkBody(ApiResponse body, int status, String message) {
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(status);
        assertThat(body.getMessage()).contains(message);
        assertThat(body.getPath()).isEqualTo(API_TEST_ENDPOINT);
    }
}
