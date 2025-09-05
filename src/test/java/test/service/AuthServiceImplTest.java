package test.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.AuthServiceImpl;
import com.example.expensetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    private JwtUtil jwtUtil;
    private LogService logService;
    private UserService userService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        logService = mock(LogService.class);
        userService = mock(UserService.class);
        authService = new AuthServiceImpl(jwtUtil, logService, userService);
    }

    @Test
    public void register_shouldSaveUserLogAndReturnTokens() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");
        User user = User.builder()
                .email(dto.getEmail())
                .password("encoded")
                .roles(Set.of(Role.USER))
                .build();

        when(userService.register(any())).thenReturn(user);
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");

        Map<String, String> result = authService.register(dto);

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(logService).log(logCaptor.capture());
        LogEntry capturedLog = logCaptor.getValue();

        assertLogEntry(user, capturedLog, "Пользователь зарегестрирован.", "/api/auth/register");
        assertThat(capturedLog.getStackTrace()).isNull();
        assertThat(result)
                .containsEntry("accessToken", "access")
                .containsEntry("refreshToken", "refresh");
        assertThat(result.get("accessToken")).isNotEqualTo(result.get("refreshToken"));
        InOrder inOrder = inOrder(userService, logService, jwtUtil);
        inOrder.verify(userService).register(dto);
        inOrder.verify(jwtUtil).generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        inOrder.verify(jwtUtil).generateRefreshToken(user.getEmail());
    }

    @Test
    public void login_shouldValidateUserLogAndReturnTokens() {
        LoginDto dto = new LoginDto("john@example.com", "password");
        User user = User.builder()
                .email(dto.getEmail())
                .password("encoded")
                .roles(Set.of(Role.USER))
                .build();

        when(userService.validateUser(any())).thenReturn(user);
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");

        Map<String, String> result = authService.login(dto);

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(logService).log(logCaptor.capture());
        LogEntry capturedLog = logCaptor.getValue();

        assertLogEntry(user, capturedLog, "Пользователь вошёл в систему.", "/api/auth/login");
        assertThat(result)
                .containsEntry("accessToken", "access")
                .containsEntry("refreshToken", "refresh");
        assertThat(result.get("accessToken")).isNotEqualTo(result.get("refreshToken"));
        InOrder inOrder = inOrder(userService, logService, jwtUtil);
        inOrder.verify(userService).validateUser(dto);
        inOrder.verify(jwtUtil).generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        inOrder.verify(jwtUtil).generateRefreshToken(user.getEmail());
    }

    private void assertLogEntry(User user, LogEntry capturedLog, String message, String path) {
        assertThat(capturedLog.getLevel()).isEqualTo("INFO");
        assertThat(capturedLog.getLogger()).isEqualTo("AuthServiceImpl");
        assertThat(capturedLog.getMessage()).contains(message);
        assertThat(capturedLog.getUser()).isEqualTo(user.getEmail());
        assertThat(capturedLog.getPath()).isEqualTo(path);
        assertThat(capturedLog.getStackTrace()).isNull();
    }
}
