package test.exception;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.exception.GlobalExceptionHandler;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogService;
import com.example.expensetracker.logging.audit.AuditAction;
import com.example.expensetracker.model.User;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

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
        request = new MockHttpServletRequest();
        request.setRequestURI(API_TEST_ENDPOINT);
    }

    @Test
    void handleBadCredentials_shouldReturnUnauthorizedResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        BadCredentialsException ex = new BadCredentialsException("");

        var response = handler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        checkBody(response.getBody(), 401, "handle.bad.credentials");
        verify(appLogService).log(any());
    }

    @Test
    void handleBadCredentials_shouldUsePrincipalNameWhenPresent() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        request.setUserPrincipal(() -> "test-user");
        BadCredentialsException ex = new BadCredentialsException("");

        var response = handler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(appLogService).log(any());
    }

    @Test
    void handleAccessDenied_shouldReturnForbiddenResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        AccessDeniedException ex = new AccessDeniedException("");

        var response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        checkBody(response.getBody(), 403, "handle.access.denied");
        verify(appLogService).log(any());
    }

    @Test
    void handleEntityNotFound_shouldReturnNotFoundResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        EntityNotFoundException ex = new EntityNotFoundException("");

        var response = handler.handleEntityNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        checkBody(response.getBody(), 404, "handle.entity.not.found");
        verify(appLogService).log(any());
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflictResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        DataIntegrityViolationException ex = new DataIntegrityViolationException("");

        var response = handler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        checkBody(response.getBody(), 409, "handle.data.integrity.violation");
        verify(appLogService).log(any());
    }

    @Test
    void handleUsernameNotFound_shouldReturnUnauthorizedResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        UsernameNotFoundException ex = new UsernameNotFoundException("");

        var response = handler.handleUsernameNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        checkBody(response.getBody(), 401, "handle.username.not.found");
        verify(appLogService).log(any());
    }

    @Test
    void handleUserNotFoundById_shouldReturnNotFoundResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        UserNotFoundByIdException ex = new UserNotFoundByIdException("");

        var response = handler.handleUserNotFoundById(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        checkBody(response.getBody(), 404, "handle.user.not.found.by.id");
        verify(appLogService).log(any());
    }

    @Test
    void handleEntityExists_shouldReturnConflictResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        EntityExistsException ex = new EntityExistsException("");

        var response = handler.handleEntityExists(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        checkBody(response.getBody(), 409, "handle.entity.exists");
        verify(appLogService).log(any());
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequestResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        IllegalArgumentException ex = new IllegalArgumentException("");

        var response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        checkBody(response.getBody(), 400, "handle.illegal.argument");
        verify(appLogService).log(any());
    }
    
    @Test
    void handleMethodArgumentNotValid_shouldReturnBadRequestResponseWithValidationMessage() {
        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "email", "Почта обязательна");
        FieldError fieldError2 = new FieldError("object", "password", "Пароль обязателен");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("Почта обязательна");
        assertThat(body.getMessage()).contains("Пароль обязателен");
        assertThat(body.getStatus()).isEqualTo(400);
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnBadRequestResponseWithDefaultMessage() {
        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("validation.error");
        assertThat(body.getStatus()).isEqualTo(400);
    }

    @Test
    void handleMethodArgumentNotValid_shouldLogPrincipalNameWhenPresent() {
        request.setUserPrincipal(() -> "user");
        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("validation.error").doesNotContain("null");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequestResponseWithValidationMessage() {
        ConstraintViolation<?> v1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> v2 = mock(ConstraintViolation.class);
        when(v1.getMessage()).thenReturn("Почта обязательна");
        when(v2.getMessage()).thenReturn("Пароль обязателен");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v1, v2));

        var response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("Почта обязательна");
        assertThat(body.getMessage()).contains("Пароль обязателен");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequestResponseWithDefaultMessage() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        ConstraintViolationException ex = new ConstraintViolationException(Set.of());

        var response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("validation.error");
    }

    @Test
    void handleGeneric_shouldReturnInternalServerErrorResponse() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        Exception ex = new RuntimeException("");

        var response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiResponse<?> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getMessage()).contains("handle.generic");
        verify(appLogService).log(any());
    }

    @Test
    void handleGeneric_shouldIncludeStackTraceFor5xxErrors() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        request.setUserPrincipal(() -> "stack-user");
        Exception ex = new RuntimeException("Exception");

        var response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        verify(appLogService).log(any());
    }

    @Test
    void handleGeneric_shouldUseClassSimpleName_whenNoStackTrace() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
        
        Exception ex = new Exception("No stack trace");
        ex.setStackTrace(new StackTraceElement[0]);

        var response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        ArgumentCaptor<AppLogDto> appLogDtoCaptor = ArgumentCaptor.forClass(AppLogDto.class);
        verify(appLogService).log(appLogDtoCaptor.capture());
        AppLogDto savedDto = appLogDtoCaptor.getValue();
        assertThat(savedDto.getErrorType()).isEqualTo("Exception");
    }

    private void checkBody(ApiResponse<?> body, int status, String message) {
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(status);
        assertThat(body.getMessage()).contains(message);
        assertThat(body.getPath()).isEqualTo(API_TEST_ENDPOINT);
    }
}
