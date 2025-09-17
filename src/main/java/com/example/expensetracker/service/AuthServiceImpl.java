package com.example.expensetracker.service;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.HashSet;

@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final JwtUtil jwtUtil;
    private final LogService logService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtUtil jwtUtil, LogService logService,
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.logService = logService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterDto dto) {
        log.debug("Попытка регистрации: {}", dto.getEmail());
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Регистрации провалилась: почта {} уже используется", dto.getEmail());
            throw new DataIntegrityViolationException("Эта почта уже используется.");
        }
        User user = User.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .roles(new HashSet<>())
                    .build();
            user.getRoles().add(Role.USER);
            User saved = userRepository.save(user);
            log.info("Успешная регистрация: {}", saved.getEmail());
        logService.log(LogEntry.builder()
                .timestamp(Instant.now())
                .level("INFO")
                .logger("AuthServiceImpl")
                .message("Пользователь зарегестрирован.")
                .user(saved.getEmail())
                .path("/api/auth/register")
                .stackTrace(null)
                .build());
        return saved;
    }

    @Override
    public TokenResponse login(LoginDto dto) {
        log.debug("Попытка авторизации: {}", dto.getEmail());
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Авторизация провалилась: почта {} не найдена", dto.getEmail());
                    return new BadCredentialsException("Неверная почта.");
                });
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Авторизация провалилась: не верный пароль для почты {}", dto.getEmail());
            throw new BadCredentialsException("Неверный пароль.");
        }
        log.info("Успешная авторизация: {}", dto.getEmail());
        logService.log(LogEntry.builder()
                .timestamp(Instant.now())
                .level("INFO")
                .logger("AuthServiceImpl")
                .message("Пользователь вошёл в систему.")
                .user(user.getEmail())
                .path("/api/auth/login")
                .stackTrace(null)
                .build());
        return generateTokens(user);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        log.debug("Попытка обновления токена");
        try {
            Jws<Claims> claims = jwtUtil.parse(request.refreshToken());
            String email = claims.getPayload().getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Неудачная попытка обновления токена, пользователь {} не найден", email);
                        return new UsernameNotFoundException("Пользователь не найден.");
                    });
            if (user.isBanned()) {
                log.warn("Неудачная попытка обновления токена, пользователь {} заблокирован", email);
                throw new AccessDeniedException("Ваш аккаунт заблокирован.");
            }
            log.info("Успешное обновление токена, пользователя {}", email);
            logService.log(LogEntry.builder()
                    .timestamp(Instant.now())
                    .level("INFO")
                    .logger("AuthServiceImpl")
                    .message("токены обновены.")
                    .user(user.getEmail())
                    .path("/api/auth/refresh")
                    .stackTrace(null)
                    .build());
            return generateTokens(user);
        } catch (UsernameNotFoundException | AccessDeniedException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка обновление токена, пользователя {}", e.getMessage(), e);
            throw new BadCredentialsException("Невалидный токен.");
        }
    }

    private TokenResponse generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return new TokenResponse(accessToken, refreshToken);
    }
}
