package com.example.expensetracker.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.User;
import com.example.expensetracker.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final LogService logService;
    private final UserService userService;

    public AuthServiceImpl(JwtUtil jwtUtil, LogService logService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.logService = logService;
        this.userService = userService;
    }

    @Override
    public Map<String, String> register(RegisterDto dto) {
        User user = userService.register(dto);
        logService.log("INFO", "Пользователь зарегестрирован.", user.getEmail(),
                "/api/auth/register");
        return generateTokens(user);
    }

    @Override
    public Map<String, String> login(LoginDto dto) {
        User user = userService.validateUser(dto);
        logService.log("INFO", "Пользователь вошёл в систему.", user.getEmail(), "/api/auth/login");
        return generateTokens(user);
    }

    private Map<String, String> generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
}
