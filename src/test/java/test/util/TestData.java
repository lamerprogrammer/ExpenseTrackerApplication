package test.util;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserResponseDto;
import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.AuditLevel;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

import static com.example.expensetracker.logging.AuditLevel.INFO;
import static test.util.Constants.*;

public class TestData {

    public static User user(String email, String password, Set<Role> roles, boolean banned) {
        return User.builder()
                .id(42L)
                .email(email)
                .password(password)
                .roles(roles)
                .banned(banned)
                .build();
    }

    public static User user() {
        return user(USER_EMAIL, USER_PASSWORD, Set.of(Role.USER), false);
    }

    public static User userBanned() {
        return user(USER_EMAIL, USER_PASSWORD, Set.of(Role.USER), true);
    }

    public static User moderator() {
        return user(MODERATOR_EMAIL, USER_PASSWORD, Set.of(Role.MODERATOR), false);
    }

    public static User admin() {
        return user(ADMIN_EMAIL, USER_PASSWORD, Set.of(Role.ADMIN), false);
    }

    public static LoginRequest loginRequest(String mail, String password) {
        return new LoginRequest(mail, password);
    }

    public static LoginRequest loginRequest() {
        return loginRequest(USER_EMAIL, USER_PASSWORD);
    }

    public static RegisterDto registerDto(String user, String mail, String password) {
        return new RegisterDto(user, mail, password);
    }

    public static RegisterDto registerDto() {
        return registerDto(USER_NAME, USER_EMAIL, USER_PASSWORD);
    }

    public static LoginDto loginDto(String mail, String password) {
        return new LoginDto(mail, password);
    }

    public static LoginDto loginDto() {
        return loginDto(USER_EMAIL, USER_PASSWORD);
    }

    public static AppLog appLog(String id,
                                Instant timestamp,
                                AuditLevel level,
                                String logger,
                                String message,
                                String userEmail,
                                String endPoint,
                                String stackTrace) {
        return new AppLog(id,
                timestamp,
                level,
                logger,
                message,
                userEmail,
                endPoint);
    }

    public static AppLog appLog() {
        return appLog("42",
                Instant.now(),
                INFO,
                "TestData",
                "Test message",
                USER_EMAIL,
                API_TEST_ENDPOINT,
                "StackTrace");
    }

    public static User user(UserRepository userRepository, PasswordEncoder encoder) {
        User user = new User();
        user.setId(42L);
        user.setEmail(USER_EMAIL);
        user.setRoles(Set.of(Role.USER));
        user.setPassword(encoder.encode(USER_PASSWORD));
        userRepository.save(user);
        return user;
    }
    
    public static UserResponseDto userResponseDto() {
        return userResponseDto(42L, USER_EMAIL);
    }

    public static UserResponseDto userResponseDto(Long id, String email) {
        return new UserResponseDto(id, email);
    }
}
