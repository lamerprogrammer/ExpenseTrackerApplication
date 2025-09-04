package test.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtUtil;
import com.example.expensetracker.service.UserService;
import com.example.expensetracker.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    public void register_shouldCreateUserWithEncodedPassword() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(dto);

        verify(userRepository).save(any(User.class));
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.getRoles()).contains(Role.USER);
    }

    @Test
    public void register_shouldThrowException_whenEmailExists() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Эта почта уже используется.");

        verify(userRepository, never()).save(any());
    }

    @Test
    public void validateUser_shouldReturnUser_whenPasswordMatches() {
        LoginDto dto = new LoginDto("exists@example.com", "password");
        User user = User.builder().email(dto.getEmail()).password("encoded").build();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), "encoded")).thenReturn(true);

        User result = userService.validateUser(dto);

        assertThat(result).isEqualTo(user);
    }

    @Test
    public void validateUser_shouldThrowException_whenUserNotFound() {
        LoginDto dto = new LoginDto("exists@example.com", "password");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.validateUser(dto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Неверная почта.");
    }

    @Test
    public void validateUser_shouldThrowException_whenPasswordDoesNotMatch() {
        LoginDto dto = new LoginDto("exists@example.com", "password");
        User user = User.builder().email(dto.getEmail()).password("encoded").build();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.validateUser(dto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Неверный пароль.");
    }

    @Test
    public void createAdmin_shouldAssignAdminRole() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createAdmin(dto);

        verify(userRepository).save(any(User.class));
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRoles()).contains(Role.ADMIN);
    }

    @Test
    public void createAdmin_shouldThrowException_whenEmailExists() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createAdmin(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Эта почта уже используется.");

        verify(userRepository, never()).save(any());
    }
}
