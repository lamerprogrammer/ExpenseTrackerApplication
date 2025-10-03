package test.util;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogLevel;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
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

    public static AppLogDto appLogDto(String id,
                                      Instant timestamp,
                                      AppLogLevel level,
                                      String logger,
                                      String errorType,
                                      String message,
                                      String userEmail,
                                      String endPoint) {
        return new AppLogDto(id,
                timestamp,
                level,
                logger,
                errorType,
                message,
                userEmail,
                endPoint);
    }

    public static AppLogDto appLogDto() {
        return appLogDto("42",
                Instant.now(),
                INFO,
                "TestData",
                "WARN",
                "Test message",
                USER_EMAIL,
                API_TEST_ENDPOINT);
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
    
    public static UserDto userResponseDto() {
        return userResponseDto(42L, USER_EMAIL);
    }

    public static UserDto userResponseDto(Long id, String email) {
        return new UserDto(id, email);
    }
}
