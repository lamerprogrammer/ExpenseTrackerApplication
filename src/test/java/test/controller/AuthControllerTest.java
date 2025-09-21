package test.controller;

import com.example.expensetracker.controller.AuthController;
import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static test.util.Constants.PASSWORD;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void refresh_shouldReturnTokens_whenCredentialsValid() {
        RefreshRequest request = mock(RefreshRequest.class);
        when(authService.refresh(request)).thenReturn(new TokenResponse("access", "refresh"));

        ResponseEntity<TokenResponse> result = authController.refresh(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().accessToken()).isEqualTo("access");
        assertThat(result.getBody().refreshToken()).isEqualTo("refresh");
        verify(authService).refresh(request);
    }

    @Test
    public void refresh_shouldThrowException_whenUserNotFound() {
        RefreshRequest request = mock(RefreshRequest.class);
        when(authService.refresh(any(RefreshRequest.class))).thenThrow(
                new UsernameNotFoundException("Пользователь не найден."));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> authController.refresh(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(authService).refresh(any(RefreshRequest.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void refresh_shouldThrowException_whenUserIsBanned() {
        RefreshRequest request = mock(RefreshRequest.class);
        when(authService.refresh(any(RefreshRequest.class))).thenThrow(
                new AccessDeniedException("Ваш аккаунт заблокирован."));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> authController.refresh(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(authService).refresh(any(RefreshRequest.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void refresh_shouldThrowException_whenTokenInvalid() {
        RefreshRequest request = mock(RefreshRequest.class);
        when(authService.refresh(any(RefreshRequest.class))).thenThrow(
                new BadCredentialsException("Невалидный токен."));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authController.refresh(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(authService).refresh(any(RefreshRequest.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void register_shouldReturnNewUser_whenCredentialsValid() {
        RegisterDto dto = TestData.registerDto();
        User user = TestData.user();
        when(authService.register(dto)).thenReturn(user);

        ResponseEntity<User> result = authController.register(dto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).usingRecursiveComparison().isEqualTo(user);
        verify(authService).register(dto);
    }

    @Test
    public void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterDto dto = TestData.registerDto();
        when(authService.register(any(RegisterDto.class))).thenThrow(
                new DataIntegrityViolationException(("Эта почта уже используется.")));

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> authController.register(dto));

        assertThat(ex.getMessage()).isNotBlank();
        verify(authService).register(any(RegisterDto.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void login_shouldReturnTokens_whenDataIsValid() {
        LoginRequest request = new LoginRequest(USER_EMAIL, PASSWORD);
        when(authService.login(any(LoginDto.class))).thenReturn(
                new TokenResponse("access", "refresh"));

        ResponseEntity<TokenResponse> result = authController.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().accessToken()).isEqualTo("access");
        assertThat(result.getBody().refreshToken()).isEqualTo("refresh");
        verify(authService).login(any(LoginDto.class));
    }

    @Test
    public void login_shouldThrowException_whenUserNotExist() {
        LoginRequest request = new LoginRequest(USER_EMAIL, PASSWORD);
        when(authService.login(any(LoginDto.class))).thenThrow(new BadCredentialsException("Неверная почта."));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authController.login(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(authService).login(any(LoginDto.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    public void login_shouldThrowException_whenPasswordNotMatches() {
        LoginRequest request = new LoginRequest(USER_EMAIL, PASSWORD);
        when(authService.login(any(LoginDto.class))).thenThrow(new BadCredentialsException("Неверный пароль."));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authController.login(request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(authService).login(any(LoginDto.class));
        verifyNoMoreInteractions(authService);
    }
}