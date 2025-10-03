package com.example.expensetracker.exception;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogLevel;
import com.example.expensetracker.logging.applog.AppLogService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;
    private final AppLogService appLogService;

    public GlobalExceptionHandler(MessageSource messageSource, AppLogService appLogService) {
        this.messageSource = messageSource;
        this.appLogService = appLogService;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleBadCredentials(BadCredentialsException ex, 
                                                                     HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, msg("handle.bad.credentials"), request, ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleAccessDenied(AccessDeniedException ex, 
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, msg("handle.access.denied"), request, ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleEntityNotFound(EntityNotFoundException ex, 
                                                                     HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, msg("handle.entity.not.found"), request, ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleDataIntegrityViolation(DataIntegrityViolationException ex, 
                                                                             HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, msg("handle.data.integrity.violation"), request, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleIllegalArgument(IllegalArgumentException ex, 
                                                                      HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, msg("handle.illegal.argument"), request, ex);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleUsernameNotFound(UsernameNotFoundException ex, 
                                                                       HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, msg("handle.username.not.found"), request, ex);
    }

    @ExceptionHandler(UserNotFoundByIdException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleUserNotFoundById(UserNotFoundByIdException ex, 
                                                                       HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, msg("handle.user.not.found.by.id"), request, ex);
    }
    
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleEntityExists(EntityExistsException ex,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, msg("handle.entity.exists"), request, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                    HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Objects.requireNonNullElse(error.getDefaultMessage(), msg("validation.error")))
                .toList();
        String message = errors.isEmpty() ? msg("validation.error") : String.join(". ", errors);
        
        log.warn("Validation error: user={} path={} errors={}",
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                request.getRequestURI(),
                errors);
        return ApiResponseFactory.validationError(HttpStatus.BAD_REQUEST, message, request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<UserDto>> handleMethodArgumentNotValid(ConstraintViolationException ex,
                                                                             HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        String message = errors.isEmpty() ? msg("validation.error") : String.join(". ", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<UserDto>> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, msg("handle.generic"),
                request, ex);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(
            HttpStatus status, String message, HttpServletRequest request, Exception ex) {

        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";

        AppLogLevel level = (ex instanceof IllegalArgumentException || ex instanceof ConstraintViolationException) ?
                AppLogLevel.WARN : AppLogLevel.ERROR;
        String logger = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0].getClassName() : 
                ex.getClass().getSimpleName();

        AppLogDto dto = new AppLogDto(
                null,
                Instant.now(),
        level,
        logger,
        ex.getClass().getSimpleName(),
        ex.getMessage(),
        user,
        request.getRequestURI()
        );
        
        appLogService.log(dto);

        if (status.is5xxServerError()) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            log.error("GlobalExceptionHandler | user={} path={} status={} message={}\n{}",
                    user, request.getRequestURI(), status.value(), message, writer);
        } else {
            log.warn("GlobalExceptionHandler: user: {} path: {} message: {}",
                    user, request.getRequestURI(), message);
        }
        return ApiResponseFactory.error(status, ex.getClass().getSimpleName(), message, request);
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
