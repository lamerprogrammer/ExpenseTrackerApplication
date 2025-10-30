package com.example.expensetracker.controller;


import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final MessageSource messageSource;

    public AuthController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest refreshRequest,
                                                 HttpServletRequest request) {
        TokenResponse response = authService.refresh(refreshRequest);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("auth.controller.refresh"), request));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterDto dto,
                                                         HttpServletRequest request) {
        User user = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("auth.controller.register"), request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                            HttpServletRequest request) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponseFactory.success(tokenResponse,
                msg("auth.controller.login"), request));
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
