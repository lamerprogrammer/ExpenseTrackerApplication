package com.example.expensetracker.controller;


import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth.tag.name", description = "auth.tag.desc")
@RestController
@RequestMapping("/api/auth")
public class AuthController implements ControllerSupport {

    private final AuthService authService;
    private final MessageSource messageSource;

    public AuthController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "auth.refresh.sum",
            description = "auth.refresh.desc")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest refreshRequest,
                                                              HttpServletRequest request) {
        TokenResponse response = authService.refresh(refreshRequest);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("auth.controller.refresh"), request));
    }

    @PostMapping("/register")
    @Operation(
            summary = "auth.register.sum",
            description = "auth.register.desc")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterDto dto,
                                                         HttpServletRequest request) {
        User user = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("auth.controller.register"), request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "auth.login.sum",
            description = "auth.login.desc")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                            HttpServletRequest request) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponseFactory.success(tokenResponse,
                msg("auth.controller.login"), request));
    }
}

