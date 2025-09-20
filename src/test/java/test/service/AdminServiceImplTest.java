package test.service;

import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogRepository auditLogRepository;
    
    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    public void createAdmin_shouldAssignAdminRole() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adminService.createAdmin(dto);

        verify(userRepository).save(any(User.class));
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRoles()).contains(Role.ADMIN);
    }

    @Test
    public void createAdmin_shouldThrowException_whenEmailExists() {
        RegisterDto dto = new RegisterDto("John", "john@example.com", "password");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Эта почта уже используется.");

        verify(userRepository, never()).save(any());
    }
}
