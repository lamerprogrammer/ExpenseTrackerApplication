package test.service;

import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.AuditAction;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.List;
import java.util.Optional;

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
    public void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(TestData.user());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = adminService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    public void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = adminService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        var response = adminService.banUser(id, currentUser);

        assertThat(response).isEqualTo(HttpStatus.OK);
        assertThat(response).isNotNull();
        assertThat(response).asString().startsWith("Пользователь").contains("заблокирован");
        verify(userRepository).save(argThat(User::isBanned));
        verify(userRepository).save(any());
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.BAN &&
                        log.getTargetUserId().equals(id) &&
                        log.getPerformedBy().equals(currentUser.getEmail())));
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = adminService.banUser(id, currentUser);

        assertThat(response).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        var response = adminService.unbanUser(id, currentUser);

        assertThat(response).isEqualTo(HttpStatus.OK);
        assertThat(response).isNotNull();
        assertThat(response).asString().startsWith("Пользователь").contains("разблокирован");
        verify(userRepository).save(argThat(user -> !user.isBanned()));
        verify(userRepository).save(any());
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.UNBAN &&
                        log.getTargetUserId().equals(id) &&
                        log.getPerformedBy().equals(currentUser.getEmail())));
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = adminService.unbanUser(id, currentUser);

        assertThat(response).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void deleteUser_shouldReturn204_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.existsById(id)).thenReturn(true);

        var response = adminService.deleteUser(id, currentUser);

        assertThat(response).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userRepository).deleteById(id);
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.DELETE &&
                        log.getTargetUserId().equals(id) &&
                        log.getPerformedBy().equals(currentUser.getEmail())));
    }

    @Test
    public void deleteUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.existsById(id)).thenReturn(false);

        var response = adminService.deleteUser(id, currentUser);

        assertThat(response).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).deleteById(any());
        verify(auditLogRepository, never()).save(any());
    }

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
