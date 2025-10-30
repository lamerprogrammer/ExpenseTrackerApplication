package test.service;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private Claims claims;
    
    @Mock
    private Jws<Claims> jwsClaims;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldSaveAndReturnUser_whenCredentialsValid() {
        RegisterDto dto = TestData.registerDto();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn(USER_PASSWORD_ENCODED);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        var result = authService.register(dto);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo(USER_PASSWORD_ENCODED);
        assertThat(result.getRoles()).contains(Role.USER);
        verify(userRepository).existsByEmail(dto.getEmail());
        verify(passwordEncoder).encode(dto.getPassword());
        verify(userRepository).save(argThat(u ->
                u.getRoles().contains(Role.USER) &&
                u.getPassword().contains(USER_PASSWORD_ENCODED) &&
                u.getEmail().equals(dto.getEmail())));
    }

    @Test
    public void register_shouldThrowException_whenUserIsAlreadyExists() {
        RegisterDto dto = TestData.registerDto();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, 
                () -> authService.register(dto));
        
        assertThat(ex.getMessage()).isNotBlank();
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void login_shouldReturnTokens_whenCredentialsValid() {
        LoginRequest dto = TestData.loginRequest();
        User user = TestData.user();
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn(TOKEN_ACCESS);
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn(TOKEN_REFRESH);

        var result = authService.login(dto);

        assertThat(result.accessToken()).isEqualTo(TOKEN_ACCESS);
        assertThat(result.refreshToken()).isEqualTo(TOKEN_REFRESH);
        verify(jwtUtil).generateAccessToken(eq(user.getEmail()), eq(Role.USER));
        verify(jwtUtil).generateRefreshToken(eq(user.getEmail()));
    }

    @Test
    public void login_shouldThrowException_whenEmailNotFound() {
        LoginRequest dto = TestData.loginRequest();
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(dto));
        
        assertThat(ex.getMessage()).isNotBlank();
        verify(passwordEncoder, never()).encode(any());
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void login_shouldThrowException_whenPasswordNotMatches() {
        LoginRequest dto = TestData.loginRequest();
        User user = TestData.user();
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(dto));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository).findByEmail(dto.email());
        verify(passwordEncoder).matches(dto.password(), user.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void refresh_shouldReturnTokens_whenRefreshTokenValid() {
        RefreshRequest request = new RefreshRequest("validToken");
        User user = TestData.user();
        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsClaims);
        when(jwsClaims.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refresh");

        var result = authService.refresh(request);

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        verify(jwtUtil).parse(request.refreshToken());
        verify(jwsClaims).getPayload();
        verify(claims).getSubject();
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(jwtUtil).generateAccessToken(eq(user.getEmail()), eq(Role.USER));
        verify(jwtUtil).generateRefreshToken(eq(user.getEmail()));
    }

    @Test
    public void refresh_shouldThrowException_whenTokenInvalid() {
        RefreshRequest request = new RefreshRequest("validToken");
        when(jwtUtil.parse(request.refreshToken())).thenThrow(new RuntimeException());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, 
                () -> authService.refresh(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil).parse(request.refreshToken());
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void refresh_shouldThrowException_whenUserNotFound() {
        RefreshRequest request = new RefreshRequest("validToken");
        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsClaims);
        when(jwsClaims.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, 
                () -> authService.refresh(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(jwtUtil).parse(request.refreshToken());
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void refresh_shouldThrowException_whenUserBanned() {
        RefreshRequest request = new RefreshRequest("validToken");
        User userBanned = TestData.userBanned();
        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsClaims);
        when(jwsClaims.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(eq(USER_EMAIL))).thenReturn(Optional.of(userBanned));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, 
                () -> authService.refresh(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(jwtUtil).parse(request.refreshToken());
        verify(userRepository).findByEmail(eq(userBanned.getEmail()));
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }
}
