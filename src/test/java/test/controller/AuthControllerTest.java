package test.controller;

import com.example.expensetracker.controller.AuthController;
import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import test.util.TestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static test.util.Constants.USER_PASSWORD;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    
    @Mock
    private AuthService authService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    MessageSource messageSource;

    @InjectMocks
    private AuthController authController;

    @Test
    public void refresh_shouldReturnTokens_whenCredentialsValid() {
        RefreshRequest refreshRequest = mock(RefreshRequest.class);
        when(authService.refresh(refreshRequest)).thenReturn(new TokenResponse("access", "refresh"));
        mockMessage();

        var result = authController.refresh(refreshRequest, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().accessToken()).isEqualTo("access");
        assertThat(body.getData().refreshToken()).isEqualTo("refresh");
        verify(authService).refresh(refreshRequest);
        verify(messageSource).getMessage(eq("auth.controller.refresh"), any(), any());
    }

    @Test
    public void register_shouldReturnNewUser_whenCredentialsValid() {
        RegisterDto dto = TestData.registerDto();
        User user = TestData.user();
        when(authService.register(dto)).thenReturn(user);
        mockMessage();

        var result = authController.register(dto, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getEmail()).isEqualTo(USER_EMAIL);
        verify(authService).register(dto);
        verify(messageSource).getMessage(eq("auth.controller.register"), any(), any());
    }

    @Test
    public void login_shouldReturnTokens_whenDataIsValid() {
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, USER_PASSWORD);
        TokenResponse response = new TokenResponse("access", "refresh");
        when(authService.login(loginRequest)).thenReturn(response);
        mockMessage();

        var result = authController.login(loginRequest, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().accessToken()).isEqualTo("access");
        assertThat(body.getData().refreshToken()).isEqualTo("refresh");
        verify(authService).login(loginRequest);
        verify(messageSource).getMessage(eq("auth.controller.login"), any(), any());
    }

    private void mockMessage() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }
}