package test.controller;

import com.example.expensetracker.controller.AdminController;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserResponseDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import test.util.TestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(TestData.user());
        when(adminService.getAllUsers()).thenReturn(users);

        List<UserResponseDto> result = adminController.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).extracting(UserResponseDto::getId, UserResponseDto::getEmail)
                .containsExactlyInAnyOrder(tuple(users.get(0).getId(), users.get(0).getEmail()));
        verify(adminService).getAllUsers();
    }

    @Test
    public void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(adminService.getAllUsers()).thenReturn(List.of());

        List<UserResponseDto> result = adminController.getAllUsers();

        assertThat(result).isEmpty();
        verify(adminService).getAllUsers();
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getId();
        when(adminService.banUser(id, currentUser)).thenReturn(bannedUser);

        var response = adminController.banUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().startsWith("Пользователь").contains("заблокирован");
        verify(adminService).banUser(any(), any(User.class));
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(adminService.banUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Пользователь не найден"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.banUser(id, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).banUser(any(), any(User.class));
        verifyNoMoreInteractions(adminService);
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getId();
        when(adminService.unbanUser(id, currentUser)).thenReturn(bannedUser);

        var response = adminController.unbanUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).asString().startsWith("Пользователь").contains("разблокирован");
        verify(adminService).unbanUser(any(), any(User.class));
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(adminService.unbanUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Пользователь не найден"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.unbanUser(id, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).unbanUser(any(), any(User.class));
        verifyNoMoreInteractions(adminService);
    }

    @Test
    public void deleteUser_shouldReturn200_whenUserExist() {
        User currentUser = TestData.user();
        User deletedUser = TestData.user();
        Long id = currentUser.getId();
        when(adminService.deleteUser(id, currentUser)).thenReturn(deletedUser);

        var response = adminController.deleteUser(id, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).deleteUser(any(), any(User.class));
    }

    @Test
    public void deleteUser_shouldReturn404_whenUserNotFound() {
        User currentUser = TestData.user();
        Long id = currentUser.getId();
        when(adminService.deleteUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Пользователь не найден"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.deleteUser(id, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).deleteUser(any(), any(User.class));
        verifyNoMoreInteractions(adminService);
    }

    @Test
    public void createAdmin_shouldNewAdmin_whenDataValid() {
        RegisterDto newAdmin = TestData.registerDto();
        User admin = TestData.admin();
        when(adminService.createAdmin(newAdmin)).thenReturn(admin);

        var response = adminController.createAdmin(newAdmin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createAdmin(any(RegisterDto.class));
    }

    @Test
    public void createAdmin_shouldThrowException_whenAdminAlreadyExists() {
        RegisterDto existAdmin = TestData.registerDto();
        when(adminService.createAdmin(existAdmin)).thenThrow(new IllegalArgumentException());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminController.createAdmin(existAdmin));


        verify(adminService).createAdmin(any(RegisterDto.class));
        verifyNoMoreInteractions(adminService);
    }
}
