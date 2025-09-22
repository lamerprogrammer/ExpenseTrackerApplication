package test.service;

import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.AuditAction;
import com.example.expensetracker.model.AuditLog;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertThat(result).extracting(User::getId, User::getEmail)
                .containsExactlyInAnyOrder(tuple(users.get(0).getId(), users.get(0).getEmail()));
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

        User result = adminService.banUser(id, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(User::isBanned));
        checkLoggerData(AuditAction.BAN, result, currentUser);
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.banUser(id, currentUser));

        assertThat(ex.getMessage()).contains("не найден");
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        User result = adminService.unbanUser(id, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(user -> !user.isBanned()));
        checkLoggerData(AuditAction.UNBAN, result, currentUser);
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.unbanUser(id, currentUser));

        assertThat(ex.getMessage()).contains("не найден");
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void deleteUser_shouldReturn204_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        User response = adminService.deleteUser(id, currentUser);

        assertThat(response).isNotNull();
        verify(userRepository).delete(any(User.class));
        checkLoggerData(AuditAction.DELETE, response, currentUser);
    }

    @Test
    public void deleteUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.deleteUser(id, currentUser));

        assertThat(ex.getMessage()).contains("не найден");
        verify(userRepository, never()).delete(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void createAdmin_shouldAssignAdminRole() {
        RegisterDto dto = TestData.registerDto();
        User currentUser = TestData.admin();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adminService.createAdmin(dto, currentUser);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRoles()).contains(Role.ADMIN);
        verify(userRepository).save(any(User.class));
        checkLoggerData(AuditAction.CREATE, result, currentUser);
    }

    @Test
    public void createAdmin_shouldThrowException_whenEmailExists() {
        RegisterDto dto = TestData.registerDto();
        User currentUser = TestData.admin();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(dto, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Эта почта уже используется.");

        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    private void checkLoggerData(AuditAction action, User targetUser, User performedBy) {
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == action &&
                        log.getTargetUser().equals(targetUser) &&
                        log.getPerformedBy().equals(performedBy)));
    }
}
