package test.controller;

import com.example.expensetracker.controller.UserController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import test.util.TestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.USER_PASSWORD;
import static test.util.Constants.USER_PASSWORD_NEW;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;
    
    @InjectMocks
    UserController userController;
    
    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser_whenUserLoggedIn() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        when(userService.getCurrentUser(any())).thenReturn(user);
        mockMessage();

        var result = userController.getCurrentUser(currentUser, request);
        
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getId()).isEqualTo(user.getId());
        assertThat(body.getData().getEmail()).isEqualTo(user.getEmail());
        assertThat(body.getMessage()).isEqualTo("user.controller.get.current.user");
        verify(userService).getCurrentUser(currentUser);
        verify(messageSource).getMessage(eq("user.controller.get.current.user"), any(), any());
    }

    @Test
    void changePassword_shouldReturnSuccessResponse() {
        ChangePasswordRequest requestDto = new ChangePasswordRequest(USER_PASSWORD, USER_PASSWORD_NEW);
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        mockMessage();

        var result = userController.changePassword(requestDto, currentUser, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("user.controller.password.changed.success");
        verify(userService).changePassword(currentUser, requestDto);
        verify(messageSource).getMessage(eq("user.controller.password.changed.success"), any(), any());
    }

    private void mockMessage() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }
}
