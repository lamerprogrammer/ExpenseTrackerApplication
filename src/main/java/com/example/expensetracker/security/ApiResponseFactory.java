package com.example.expensetracker.security;

import com.example.expensetracker.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ApiResponseFactory {

    public ApiResponse unauthorized(String path) {
        return new ApiResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Доступ запрещён: требуется авторизация.",
                path);

    }

    public ApiResponse forbidden(String path) {
        return new ApiResponse(Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "У вас недостаточно прав для доступа к этому ресурсу.",
                path);
    }
}
