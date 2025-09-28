package com.example.expensetracker.dto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

public class ApiResponseFactory {
    public static <T> ApiResponse<T> success(T data, String message, HttpServletRequest request) {
        return new ApiResponse<>(
                Instant.now(),
                HttpStatus.OK.value(),
                null,
                message,
                request.getRequestURI(),
                data,
                null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String error, String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ApiResponse<>(
                        Instant.now(),
                        status.value(),
                        error,
                        message,
                        request.getRequestURI(),
                        null,
                        null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> validationError(HttpStatus status, String message,
                                                           HttpServletRequest request, List<String> errors) {
        return ResponseEntity.status(status).body(
                new ApiResponse<>(
                        Instant.now(),
                        status.value(),
                        "ValidationError",
                        message,
                        request.getRequestURI(),
                        null,
                        errors));
    }

    public static ApiResponse<?> unauthorized(HttpServletRequest request) {
        return new ApiResponse<>(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Доступ запрещён: требуется авторизация.",
                request.getRequestURI(),
                null,
                null
        );
    }

    public static ApiResponse<?> forbidden(HttpServletRequest request) {
        return new ApiResponse<>(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "У вас недостаточно прав для доступа к этому ресурсу.",
                request.getRequestURI(),
                null,
                null
        );
    }
}
