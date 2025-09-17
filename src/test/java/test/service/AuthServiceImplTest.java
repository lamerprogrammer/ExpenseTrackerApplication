package test.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.TokenResponse;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.logging.LogService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LogService logService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder encoder;
    
    @InjectMocks
    private AuthService authService;

    @Test
    public void register_shouldSaveUser_when() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");
        User user = User.builder()
                .email(dto.getEmail())
                .password("encoded")
                .roles(Set.of(Role.USER))
                .build();

        when(authService.register(any())).thenReturn(user);
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");

        User result = authService.register(dto);

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(logService).log(logCaptor.capture());
        LogEntry capturedLog = logCaptor.getValue();

        assertLogEntry(user, capturedLog, "Пользователь зарегестрирован.", "/api/auth/register");
        assertThat(capturedLog.getStackTrace()).isNull();
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo(dto.getPassword());
        InOrder inOrder = inOrder(authService, logService, jwtUtil);
        inOrder.verify(authService).register(dto);
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
        
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");

        TokenResponse result = authService.login(dto);

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(logService).log(logCaptor.capture());
        LogEntry capturedLog = logCaptor.getValue();

        assertLogEntry(user, capturedLog, "Пользователь вошёл в систему.", "/api/auth/login");
        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        InOrder inOrder = inOrder(authService, logService, jwtUtil);
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
