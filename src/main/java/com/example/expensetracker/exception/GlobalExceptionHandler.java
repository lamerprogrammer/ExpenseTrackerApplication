package com.example.expensetracker.exception;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final LogService logService;

    public GlobalExceptionHandler(LogService logService) {
        this.logService = logService;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "WARN",
                "Неверные учётные данные.", request, ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "WARN",
                "Отказано в доступе.", request, ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "WARN",
                "Запрошенный ресурс не найден.", request, ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "WARN",
                "Нарушение ограничений базы данных.", request, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "WARN",
                "Некорректный запрос.", request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "ERROR",
                ex.getMessage(), request, ex);
    }

    private ResponseEntity<ApiResponse> buildResponse(
            HttpStatus status, String message, String logLevel, String logMessage, HttpServletRequest request, Exception ex) {

        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";

        String stackTrace = null;
        if (status.is5xxServerError()) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            stackTrace = writer.toString();
        }

        try {
            logService.log(LogEntry.builder()
                    .timestamp(Instant.now())
                    .level(logLevel)
                    .logger("GlobalExceptionHandler")
                    .message(logMessage)
                    .user(user)
                    .path(request.getRequestURI())
                    .stackTrace(stackTrace)
                    .build());
        } catch (Exception e) {
            logService.log(LogEntry.builder()
                    .timestamp(Instant.now())
                    .level("ERROR")
                    .logger("GlobalExceptionHandler")
                    .message(logMessage)
                    .user(user)
                    .path(request.getRequestURI())
                    .stackTrace(e.getMessage())
                    .build());
        }

        ApiResponse response = new ApiResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }
}
