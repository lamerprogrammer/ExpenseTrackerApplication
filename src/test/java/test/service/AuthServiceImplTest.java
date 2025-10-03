//package test.service;
//
//import com.example.expensetracker.dto.LoginDto;
//import com.example.expensetracker.dto.RefreshRequest;
//import com.example.expensetracker.dto.RegisterDto;
//import com.example.expensetracker.dto.TokenResponse;
//import com.example.expensetracker.logging.LogEntry;
//import com.example.expensetracker.logging.LogService;
//import com.example.expensetracker.model.Role;
//import com.example.expensetracker.model.User;
//import com.example.expensetracker.repository.UserRepository;
//import com.example.expensetracker.security.JwtUtil;
//import com.example.expensetracker.service.AuthServiceImpl;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.MessageSource;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import test.util.TestData;
//
//import java.util.Optional;
//
//import static com.example.expensetracker.logging.audit.AuditLevel.INFO;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//import static test.util.Constants.USER_EMAIL;
//
//@ExtendWith(MockitoExtension.class)
//public class AuthServiceImplTest {
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @Mock
//    private LogService logService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private MessageSource messageSource;
//
//    @InjectMocks
//    private AuthServiceImpl authService;
//
//    @Test
//    void register_shouldSaveAndReturnUser_whenCredentialsValid() {
//        RegisterDto dto = TestData.registerDto();
//
//        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
//        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
//        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
//
//        User result = authService.register(dto);
//
//        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
//        verify(logService).log(logCaptor.capture());
//        LogEntry capturedLog = logCaptor.getValue();
//
//        assertLogEntry(capturedLog, "Пользователь зарегестрирован.", "/api/auth/register");
//        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
//        assertThat(result.getPassword()).isEqualTo("encoded");
//        assertThat(result.getRoles()).containsExactly(Role.USER);
//        verify(userRepository).existsByEmail(dto.getEmail());
//        verify(passwordEncoder).encode(dto.getPassword());
//        verify(userRepository).save(any(User.class));
//    }
//
//    @Test
//    public void register_shouldThrowException_whenUserIsAlreadyExists() {
//        RegisterDto dto = TestData.registerDto();
//
//        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArgument(0));
//        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
//
//        assertThrows(DataIntegrityViolationException.class, () -> authService.register(dto));
//        verify(passwordEncoder, never()).encode(any());
//        verify(logService, never()).log(any());
//        verify(userRepository, never()).save(any(User.class));
//    }
//
//    @Test
//    public void login_shouldReturnTokens_whenCredentialsValid() {
//        LoginDto dto = TestData.loginDto();
//        User user = TestData.user();
//        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(true);
//        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
//        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");
//
//        TokenResponse result = authService.login(dto);
//
//        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
//        verify(logService).log(logCaptor.capture());
//        LogEntry capturedLog = logCaptor.getValue();
//
//        assertLogEntry(user, capturedLog, "Пользователь вошёл в систему.", "/api/auth/login");
//        assertThat(result.accessToken()).isEqualTo("access");
//        assertThat(result.refreshToken()).isEqualTo("refresh");
//        verify(jwtUtil).generateAccessToken(eq(user.getEmail()), eq(Role.USER));
//        verify(jwtUtil).generateRefreshToken(eq(user.getEmail()));
//    }
//
//    @Test
//    public void login_shouldThrowException_whenEmailNotFound() {
//        LoginDto dto = TestData.loginDto();
//        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
//        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArgument(0));
//
//        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
//
//        verify(passwordEncoder, never()).encode(any());
//        verify(logService, never()).log(any());
//        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
//        verify(jwtUtil, never()).generateRefreshToken(anyString());
//    }
//
//    @Test
//    public void login_shouldThrowException_whenPasswordNotMatches() {
//        LoginDto dto = TestData.loginDto();
//        User user = TestData.user();
//        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(false);
//        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArgument(0));
//
//        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
//
//        verify(passwordEncoder, never()).encode(any());
//        verify(logService, never()).log(any());
//        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
//        verify(jwtUtil, never()).generateRefreshToken(anyString());
//    }
//
//    @Test
//    public void refresh_shouldReturnTokens_whenRefreshTokenValid() {
//        Claims claims = mock(Claims.class);
//        Jws<Claims> jwsClaims = mock(Jws.class);
//        RefreshRequest request = new RefreshRequest("validToken");
//        User user = TestData.user();
//        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsClaims);
//        when(jwsClaims.getPayload()).thenReturn(claims);
//        when(claims.getSubject()).thenReturn(USER_EMAIL);
//        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
//        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
//        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");
//
//        TokenResponse result = authService.refresh(request);
//
//        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
//        verify(logService).log(logCaptor.capture());
//        LogEntry capturedLog = logCaptor.getValue();
//
//        assertLogEntry(capturedLog, "Токены обновлены.", "/api/auth/refresh");
//        assertThat(result.accessToken()).isEqualTo("access");
//        assertThat(result.refreshToken()).isEqualTo("refresh");
//        verify(jwtUtil).parse(request.refreshToken());
//        verify(userRepository).findByEmail(user.getEmail());
//        verify(jwtUtil).generateAccessToken(eq(user.getEmail()), eq(Role.USER));
//        verify(jwtUtil).generateRefreshToken(eq(user.getEmail()));
//    }
//
//    @Test
//    public void refresh_shouldThrowException_whenTokenInvalid() {
//        RefreshRequest request = new RefreshRequest("validToken");
//        when(jwtUtil.parse(request.refreshToken())).thenThrow(new RuntimeException());
//        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArgument(0));
//
//        assertThrows(BadCredentialsException.class, () -> authService.refresh(request));
//
//        verify(jwtUtil).parse(request.refreshToken());
//        verify(logService, never()).log(any());
//        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
//        verify(jwtUtil, never()).generateRefreshToken(anyString());
//    }
//
//    @Test
//    public void refresh_shouldThrowException_whenUserNotFound() {
//        Claims claims = mock(Claims.class);
//        Jws<Claims> jwsClaims = mock(Jws.class);
//        RefreshRequest request = new RefreshRequest("validToken");
//        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsClaims);
//        when(jwsClaims.getPayload()).thenReturn(claims);
//        when(claims.getSubject()).thenReturn(USER_EMAIL);
//        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
//        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArgument(0));
//
//        assertThrows(UsernameNotFoundException.class, () -> authService.refresh(request));
//
//        verify(jwtUtil).parse(request.refreshToken());
//        verify(logService, never()).log(any());
//        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
//        verify(jwtUtil, never()).generateRefreshToken(anyString());
//    }
//
//    @Test
//    public void refresh_shouldThrowException_whenUserBanned() {
//        Claims claims = mock(Claims.class);
//        Jws<Claims> jwsClaims = mock(Jws.class);
//        RefreshRequest request = new RefreshRequest("validToken");
//        User userBanned = TestData.userBanned();
//        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsClaims);
//        when(jwsClaims.getPayload()).thenReturn(claims);
//        when(claims.getSubject()).thenReturn(USER_EMAIL);
//        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(userBanned));
//        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(i -> i.getArgument(0));
//
//        assertThrows(AccessDeniedException.class, () -> authService.refresh(request));
//
//        verify(jwtUtil).parse(request.refreshToken());
//        verify(userRepository).findByEmail(userBanned.getEmail());
//        verify(logService, never()).log(any());
//        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
//        verify(jwtUtil, never()).generateRefreshToken(anyString());
//    }
//
//    private void assertLogEntry(LogEntry capturedLog, String message, String path) {
//        assertThat(capturedLog.getLevel()).isEqualTo(INFO);
//        assertThat(capturedLog.getLogger()).isEqualTo("AuthServiceImpl");
//        assertThat(capturedLog.getMessage()).contains(message);
//        assertThat(capturedLog.getPath()).isEqualTo(path);
//    }
//
//    private void assertLogEntry(User user, LogEntry capturedLog, String message, String path) {
//        assertThat(capturedLog.getLevel()).isEqualTo(INFO);
//        assertThat(capturedLog.getLogger()).isEqualTo("AuthServiceImpl");
//        assertThat(capturedLog.getMessage()).contains(message);
//        assertThat(capturedLog.getUser()).isEqualTo(user.getEmail());
//        assertThat(capturedLog.getPath()).isEqualTo(path);
//    }
//}
