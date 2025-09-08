package test.controller;

import com.example.expensetracker.controller.AuthController;
import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RefreshRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.TokenResponse;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    public void refresh_shouldReturnNewTokens_whenUserExistsAndNotBanned() {
        RefreshRequest request = new RefreshRequest("refresh-token");

        @SuppressWarnings("unchecked")
        Jws<Claims> jwsMock = mock(Jws.class);
        Claims claimsMock = mock(Claims.class);
        User user = TestData.user();

        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsMock);
        when(jwsMock.getPayload()).thenReturn(claimsMock);
        when(claimsMock.getSubject()).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("new-refresh");

        ResponseEntity<TokenResponse> response = authController.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().refreshToken()).isEqualTo("new-refresh");
        assertThat(response.getBody().accessToken()).isEqualTo("new-access");
    }

    @Test
    public void refresh_shouldThrowException_whenUserNotFound() {
        RefreshRequest request = new RefreshRequest("refresh-token");

        @SuppressWarnings("unchecked")
        Jws<Claims> jwsMock = mock(Jws.class);
        Claims claimsMock = mock(Claims.class);
        String email = "john@example.com";

        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsMock);
        when(jwsMock.getPayload()).thenReturn(claimsMock);
        when(claimsMock.getSubject()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<TokenResponse> response = authController.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void refresh_shouldReturnForbidden_whenUserIsBanned() {
        RefreshRequest request = new RefreshRequest("refresh-token");

        @SuppressWarnings("unchecked")
        Jws<Claims> jwsMock = mock(Jws.class);
        Claims claimsMock = mock(Claims.class);
        String email = "john@example.com";
        User userBanned = TestData.userBanned();

        when(jwtUtil.parse(request.refreshToken())).thenReturn(jwsMock);
        when(jwsMock.getPayload()).thenReturn(claimsMock);
        when(claimsMock.getSubject()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userBanned));

        ResponseEntity<TokenResponse> response = authController.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void refresh_shouldReturnUnauthorized_whenTokenInvalid() {
        RefreshRequest request = new RefreshRequest("invalid-token");
        when(jwtUtil.parse(request.refreshToken())).thenThrow(new RuntimeException("Невалидный токен."));

        ResponseEntity<TokenResponse> response = authController.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void register_shouldReturnNewUser() {
        RegisterDto dto = TestData.registerDto();
        User user = TestData.user();
        when(userService.register(dto)).thenReturn(user);

        ResponseEntity<User> result = authController.register(dto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(user);
        verify(userService, times(1)).register(dto);
    }

    @Test
    public void login_shouldReturnNewTokens_whenDataIsValid() {
        LoginRequest request = TestData.loginRequest();
        User user = TestData.user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(user.getEmail(), user.getRoles().iterator().next())).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("new-refresh");

        ResponseEntity<TokenResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("new-access");
        assertThat(response.getBody().refreshToken()).isEqualTo("new-refresh");
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(passwordEncoder, times(1)).matches(request.password(), user.getPassword());
        verify(jwtUtil, times(1)).generateAccessToken(user.getEmail(), user.getRoles().iterator().next());
        verify(jwtUtil, times(1)).generateRefreshToken(user.getEmail());
    }

    @Test
    public void login_shouldThrowRuntimeException_whenUserNotExist() {
        LoginRequest request = TestData.loginRequest();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.login(request));

        assertThat(ex.getMessage()).isEqualTo("Недействительные учетные данные.");
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }

    @Test
    public void login_shouldThrowRuntimeException_whenPasswordNotMatches() {
        LoginRequest request = TestData.loginRequest();
        User user = TestData.user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.login(request));

        assertThat(ex.getMessage()).isEqualTo("Недействительные учетные данные.");
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(passwordEncoder, times(1)).matches(request.password(), user.getPassword());
        verify(jwtUtil, never()).generateAccessToken(anyString(), any());
        verify(jwtUtil, never()).generateRefreshToken(anyString());
    }
}