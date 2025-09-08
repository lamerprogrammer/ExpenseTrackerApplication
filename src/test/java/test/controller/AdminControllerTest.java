package test.controller;

import com.example.expensetracker.controller.AdminController;
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
public class AdminControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AdminController adminController;

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(TestData.user());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = adminController.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(users);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        Long id = 42L;
        User currentUser = TestData.user();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        var response = adminController.banUser(id, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().contains("заблокирован");
        verify(userRepository).save(argThat(User::isBanned));
        verify(userRepository, times(1)).save(any());
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        Long id = 99L;
        User currentUser = TestData.user();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = adminController.banUser(id, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        Long id = 42L;
        User currentUser = TestData.user();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser));

        var response = adminController.unbanUser(id, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().contains("разблокирован");
        verify(userRepository).save(argThat(user -> !user.isBanned()));
        verify(userRepository, times(1)).save(any());
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        Long id = 99L;
        User currentUser = TestData.user();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = adminController.unbanUser(id, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void deleteUser_shouldReturn204_whenUserExist() {
        Long id = 42L;
        User currentUser = TestData.user();
        when(userRepository.existsById(id)).thenReturn(true);

        var response = adminController.deleteUser(id, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userRepository, times(1)).deleteById(id);
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    public void deleteUser_shouldReturn404_whenUserNotFound() {
        Long id = 99L;
        User currentUser = TestData.user();
        when(userRepository.existsById(id)).thenReturn(false);

        var response = adminController.deleteUser(id, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).deleteById(any());
        verify(auditLogRepository, never()).save(any());
    }
}
