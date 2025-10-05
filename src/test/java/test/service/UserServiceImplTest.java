package test.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.UserService;
import com.example.expensetracker.service.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(userRepository);
    }
    
    @Test
    void getCurrentUser_shouldReturnUser_whenUserExists() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        String email = details.getUsername();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        var result = userService.getCurrentUser(details);
        
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail(email);
        assertThat(result.getEmail()).isEqualTo(details.getDomainUser().getEmail());
    }

    @Test
    void getCurrentUser_shouldThrowException_whenUserNotExists() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        String email = details.getUsername();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userService.getCurrentUser(details));
        
        assertThat(ex).isInstanceOf(UsernameNotFoundException.class);
        assertThat(ex.getMessage()).contains(email);
        verify(userRepository).findByEmail(email);
    }
}
