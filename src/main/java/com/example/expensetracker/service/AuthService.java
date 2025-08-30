package com.example.expensetracker.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LogService logService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       LogService logService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.logService = logService;
    }

    public Map<String, String> register(RegisterDto dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            logService.log("WARN", "Попытка регистрации на уже занятую почту", dto.getEmail(),
                    "/api/auth/register");
            throw new IllegalArgumentException("Почта уже используется.");
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);

        logService.log("INFO", "Пользователь зарегестрирован", user.getEmail(),
                "/api/auth/register");

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public Map<String, String> login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    logService.log("WARN", "Попытка входа с несуществующей почтой", dto.getEmail(),
                            "/api/auth/login");
                    return new IllegalArgumentException("Неверный адрес почты.");
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            logService.log("WARN", "Попытка входа с неверным паролем", dto.getEmail(),
                    "/api/auth/login");
            throw new IllegalArgumentException("Неверный пароль.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        logService.log("INFO", "Пользователь вошёл в систему", user.getEmail(), "/api/auth/login");

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
}
