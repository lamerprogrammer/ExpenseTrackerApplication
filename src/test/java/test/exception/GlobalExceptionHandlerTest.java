package test.exception;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.exception.GlobalExceptionHandler;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private LogService logService;
    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        logService = mock(LogService.class);
        handler = new GlobalExceptionHandler(logService);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    public void handleBadCredentials_shouldReturnUnauthorizedResponse() {
        BadCredentialsException ex = new BadCredentialsException("User unauthorized");

        var response = handler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        checkBody(response.getBody(), 401, "User unauthorized");

        verify(logService).log(any());
    }

    @Test
    public void handleBadCredentials_shouldUsePrincipalNameWhenPresent() {
        request.setUserPrincipal(() -> "test-user");
        BadCredentialsException ex = new BadCredentialsException("User unauthorized");

        var response = handler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        verify(logService).log(argThat(logEntry ->
                logEntry.getUser().equals("test-user")));
    }

    @Test
    public void handleAccessDenied_shouldReturnForbiddenResponse() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        var response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        checkBody(response.getBody(), 403, "Access denied");

        verify(logService).log(any());
    }

    @Test
    public void handleEntityNotFound_shouldReturnNotFoundResponse() {
        EntityNotFoundException ex = new EntityNotFoundException("User not found");

        var response = handler.handleEntityNotFound(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        checkBody(response.getBody(), 404, "User not found");

        verify(logService).log(any());
    }

    @Test
    public void handleDataIntegrityViolation_shouldReturnConflictResponse() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data integrity violation");

        var response = handler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        checkBody(response.getBody(), 409, "Data integrity violation");

        verify(logService).log(any());
    }

    @Test
    public void handleIllegalArgument_shouldReturnBadRequestResponse() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad Request");

        var response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        checkBody(response.getBody(), 400, "Bad Request");

        verify(logService).log(any());
    }

    @Test
    public void handleGeneric_shouldReturnInternalServerErrorResponse() {
        Exception ex = new RuntimeException("Unexpected error");

        var response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ApiResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getMessage()).contains("Unexpected error");

        verify(logService).log(any());
    }

    @Test
    public void handleGeneric_shouldIncludeStackTraceFor5xxErrors() {
        request.setUserPrincipal(() -> "stack-user");
        Exception ex = new RuntimeException("Exception");

        var response = handler.handleGeneric(ex, request);

        verify(logService).log(argThat(logEntry ->
                logEntry.getStackTrace() != null && logEntry.getStackTrace().contains("RuntimeException")));
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    public void buildResponse_shouldFallBackToErrorLog_whenLogServiceFails() {
        doThrow(new RuntimeException("Log failed")).doNothing().when(logService).log(any());

        BadCredentialsException ex = new BadCredentialsException("User unauthorized");

        var response = handler.handleBadCredentials(ex, request);

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(logService, times(2)).log(logCaptor.capture());
        LogEntry capturedLog = logCaptor.getValue();

        assertThat(capturedLog.getLevel()).isEqualTo("ERROR");
        assertThat(capturedLog.getStackTrace()).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    private void checkBody(ApiResponse body, int status, String message) {
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(status);
        assertThat(body.getMessage()).contains(message);
        assertThat(body.getPath()).isEqualTo("/api/test");
    }
}
