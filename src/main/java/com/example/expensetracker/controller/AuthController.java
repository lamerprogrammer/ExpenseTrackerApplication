package com.example.expensetracker.controller;


import com.example.expensetracker.dto.*;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final LogService logService;

    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, AuthService authService, LogService logService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.logService = logService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            Jws<Claims> claims = jwtUtil.parse(request.refreshToken());
            String email = claims.getPayload().getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден."));
            if (user.isBanned()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            String newAccess = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
            String newRefresh = jwtUtil.generateRefreshToken(user.getEmail());

            return ResponseEntity.ok(new TokenResponse(newAccess, newRefresh));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterDto dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Недействительные учетные данные."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Недействительные учетные данные.");
        }
        String access = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        String refresh = jwtUtil.generateRefreshToken(user.getEmail());
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }
}
