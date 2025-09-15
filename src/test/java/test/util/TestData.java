package test.util;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;

import java.time.Instant;
import java.util.Set;

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
        return user(USER_EMAIL, PASSWORD, Set.of(Role.USER), false);
    }

    public static User userBanned() {
        return user(USER_EMAIL, PASSWORD, Set.of(Role.USER), true);
    }

    public static User moderator() {
        return user(MODERATOR_EMAIL, PASSWORD, Set.of(Role.MODERATOR), false);
    }

    public static User admin() {
        return user(ADMIN_EMAIL, PASSWORD, Set.of(Role.ADMIN), false);
    }

    public static LoginRequest loginRequest(String mail, String password) {
        return new LoginRequest(mail, password);
    }

    public static LoginRequest loginRequest() {
        return loginRequest(USER_EMAIL, PASSWORD);
    }

    public static RegisterDto registerDto(String user, String mail, String password) {
        return new RegisterDto(user, mail, password);
    }

    public static RegisterDto registerDto() {
        return registerDto(USER_NAME, USER_EMAIL, PASSWORD);
    }

    public static LoginDto loginDto(String mail, String password) {
        return new LoginDto(mail, password);
    }

    public static LoginDto loginDto() {
        return loginDto(USER_EMAIL, PASSWORD);
    }

    public static AppLog appLog(String id,
                                Instant timestamp,
                                String level,
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
                endPoint,
                stackTrace);
    }

    public static AppLog appLog() {
        return appLog("42",
                Instant.now(),
                "INFO",
                "TestData",
                "Test message",
                USER_EMAIL,
                API_TEST_ENDPOINT,
                "StackTrace");
    }
}
