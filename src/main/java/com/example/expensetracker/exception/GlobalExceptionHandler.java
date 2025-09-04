package com.example.expensetracker.exception;

import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.logging.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LogService logService;

    public GlobalExceptionHandler(LogService logService) {
        this.logService = logService;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "WARN",
                "Неверные учётные данные.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "WARN",
                "Отказано в доступе.", request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "WARN",
                "Запрошенный ресурс не найден.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "WARN",
                "Нарушение ограничений базы данных.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "WARN",
                "Некорректный запрос.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "ERROR",
                ex.getMessage(), request);
    }

    private ResponseEntity<ApiResponse> buildResponse(
            HttpStatus status, String message, String logLevel, String logMessage, HttpServletRequest request) {

        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";

        String stackTrace = null;
        if (status.is5xxServerError() || "ERROR".equalsIgnoreCase(logLevel)) {
            StringWriter writer = new StringWriter();
            new Exception(message).printStackTrace(new PrintWriter(writer));
            stackTrace = writer.toString();
        }

        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", Instant.now().toString());
            logEntry.put("level", logLevel);
            logEntry.put("logger", "GlobalExceptionHandler");
            logEntry.put("message", logMessage);
            logEntry.put("user", user);
            logEntry.put("path", request.getRequestURI());
            if (stackTrace != null) {
                logEntry.put("stacktrace", stackTrace);
            }
            String jsonLog = objectMapper.writeValueAsString(logEntry);
            logService.log(logLevel, jsonLog, user, request.getRequestURI());
        } catch (Exception e) {
            logService.log("ERROR", logMessage, user, request.getRequestURI());
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
