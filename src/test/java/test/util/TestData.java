package test.util;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.logging.applog.AppLog;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogLevel;
import com.example.expensetracker.logging.audit.AuditAction;
import com.example.expensetracker.logging.audit.AuditDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
import static com.example.expensetracker.model.Role.*;
import static test.util.Constants.*;

public class TestData {

    public static User user(Long id, String email, String password, Set<Role> roles, boolean banned) {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .roles(roles)
                .banned(banned)
                .build();
    }

    public static User user() {
        return user(ID_VALID, USER_EMAIL, USER_PASSWORD, new HashSet<Role>(Set.of(USER)), false);
    }

    public static User userBanned() {
        return user(ID_VALID, USER_EMAIL, USER_PASSWORD, Set.of(USER), true);
    }

    public static User moderator() {
        return user(ID_VALID, MODERATOR_EMAIL, USER_PASSWORD, new HashSet<Role>(Set.of(USER, MODERATOR)), false);
    }

    public static User admin() {
        return user(ID_ADMIN, ADMIN_EMAIL, USER_PASSWORD, Set.of(ADMIN), false);
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
                                AppLogLevel level,
                                String logger,
                                String errorType,
                                String message,
                                String userEmail,
                                String endPoint) {
        return new AppLog(id,
                timestamp,
                level,
                logger,
                errorType,
                message,
                userEmail,
                endPoint);
    }

    public static AppLog appLog() {
        return appLog("42",
                Instant.now(),
                INFO,
                LOGGER_TEST_DATA,
                TYPE_ERROR_WARN,
                TEST_MESSAGE,
                USER_EMAIL,
                API_TEST_ENDPOINT);
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
                LOGGER_TEST_DATA,
                "WARN",
                TEST_MESSAGE,
                USER_EMAIL,
                API_TEST_ENDPOINT);
    }
    
    public static Page<AppLogDto> appLogDtoPage() {
        return new PageImpl<>(List.of(appLogDto()));
    }

    public static User user(UserRepository userRepository, PasswordEncoder encoder) {
        User user = new User();
        user.setId(42L);
        user.setEmail(USER_EMAIL);
        user.setRoles(Set.of(USER));
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

    public static AuditDto auditDto(
            Long id, 
            AuditAction action, 
            String targetUserEmail, 
            String performedByEmail,
            Instant timeStamp) {
        return new AuditDto(id, action, targetUserEmail, performedByEmail, timeStamp);
    }

    public static AuditDto auditDto() {
        return auditDto(ID_VALID,
                AuditAction.BAN,
                USER_EMAIL,
                ADMIN_EMAIL,
                Instant.now());
    }

    public static Page<AuditDto> auditDtoPage() {
        return new PageImpl<>(List.of(auditDto()));
    }
}
