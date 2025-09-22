package test.controller;

import com.example.expensetracker.controller.ModeratorController;
import com.example.expensetracker.model.AuditAction;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import test.util.TestData;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModeratorControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    ModeratorController moderatorController;

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(TestData.user());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = moderatorController.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    public void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = moderatorController.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        var response = moderatorController.banUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().startsWith("Пользователь").contains("заблокирован");
        verify(userRepository).save(argThat(User::isBanned));
        verify(userRepository).save(any());
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.BAN &&
                        log.getTargetUser().equals(id) &&
                        log.getPerformedBy().equals(currentUser.getEmail())));
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = moderatorController.banUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        var response = moderatorController.unbanUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().startsWith("Пользователь").contains("разблокирован");
        verify(userRepository).save(argThat(user -> !user.isBanned()));
        verify(userRepository).save(any());
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.UNBAN &&
                log.getTargetUser().equals(id) &&
                log.getPerformedBy().equals(currentUser.getEmail())));
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = moderatorController.unbanUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }
}
