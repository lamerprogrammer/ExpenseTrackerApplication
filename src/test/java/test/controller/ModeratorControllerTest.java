package test.controller;

import com.example.expensetracker.controller.ModeratorController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.model.AuditAction;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
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
    
    @Mock
    HttpServletRequest httpServletRequest;

    @InjectMocks
    ModeratorController moderatorController;

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(TestData.user());
        when(userRepository.findAll()).thenReturn(users);

        var result = moderatorController.getAllUsers(httpServletRequest);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isNotNull();
        assertThat(result.getBody().getData()).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    public void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        var result = moderatorController.getAllUsers(httpServletRequest);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser.getDomainUser()));

        var response = moderatorController.banUser(id, currentUser, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().startsWith("Пользователь").contains("заблокирован");
        verify(userRepository).save(argThat(User::isBanned));
        verify(userRepository).save(any());
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.BAN &&
                        log.getTargetUser().equals(id) &&
                        log.getPerformedBy().equals(currentUser.getDomainUser().getEmail())));
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = moderatorController.banUser(id, currentUser, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(currentUser.getDomainUser()));

        var response = moderatorController.unbanUser(id, currentUser, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().startsWith("Пользователь").contains("разблокирован");
        verify(userRepository).save(argThat(user -> !user.isBanned()));
        verify(userRepository).save(any());
        verify(auditLogRepository).save(argThat(log ->
                log.getAction() == AuditAction.UNBAN &&
                log.getTargetUser().equals(id) &&
                log.getPerformedBy().equals(currentUser.getDomainUser().getEmail())));
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var response = moderatorController.unbanUser(id, currentUser, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }
}
