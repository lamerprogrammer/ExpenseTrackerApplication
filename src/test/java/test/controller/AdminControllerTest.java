package test.controller;

import com.example.expensetracker.controller.AdminController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AdminService;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private HttpServletRequest request;

    @Mock
    private MessageSource messageSource;
    
    @Mock
    private Pageable pageable;

    @InjectMocks
    private AdminController adminController;
    
    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(adminService);
    }

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        User user = TestData.user();
        Page<User> users = new PageImpl<>(List.of(user));
        when(adminService.getAllUsers(pageable)).thenReturn(users);

        var result = adminController.getAllUsers(pageable, request);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getContent())
                .extracting(UserDto::getId, UserDto::getEmail)
                .containsExactly(tuple(user.getId(), user.getEmail()));
        verify(adminService).getAllUsers(pageable);
    }
    
    @Test
    void getUserById_shouldReturn200_whenUserExists() {
        User user = TestData.user();
        Long id = user.getId();
        when(adminService.getUserById(id)).thenReturn(user);
        
        var result = adminController.getUserById(id, request);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(user.getId());
        assertThat(result.getBody().getData().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getUserById_shouldReturn404_whenUserNotFound() {
        User user = TestData.user();
        Long id = user.getId();
        when(adminService.getUserById(id)).thenThrow(new UserNotFoundByIdException("Test message"));

        UserNotFoundByIdException ex = assertThrows(UserNotFoundByIdException.class,
                () -> adminController.getUserById(id, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).getUserById(id);
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.banUser(id, currentUser)).thenReturn(bannedUser);

        var response = adminController.banUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).banUser(eq(id), eq(currentUser));
    }

    @Test
    public void banUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(adminService.banUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Test message"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.banUser(id, currentUser, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).banUser(eq(id), eq(currentUser));
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.unbanUser(id, currentUser)).thenReturn(bannedUser);

        var response = adminController.unbanUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).unbanUser(eq(id), eq(currentUser));
    }

    @Test
    public void unbanUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(adminService.unbanUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Test message"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.unbanUser(id, currentUser, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).unbanUser(eq(id), eq(currentUser));
    }

    @Test
    public void promoteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User user = TestData.user();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.promoteUser(id, currentUser)).thenReturn(user);

        var response = adminController.promoteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).promoteUser(eq(id), eq(currentUser));
    }

    @Test
    public void promoteUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(adminService.promoteUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Test message"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.promoteUser(id, currentUser, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).promoteUser(eq(id), eq(currentUser));
    }

    @Test
    public void demoteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.demoteUser(id, currentUser)).thenReturn(bannedUser);

        var response = adminController.demoteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).demoteUser(eq(id), eq(currentUser));
    }

    @Test
    public void demoteUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(adminService.demoteUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Test message"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.demoteUser(id, currentUser, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).demoteUser(eq(id), eq(currentUser));
    }

    @Test
    public void deleteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User deletedUser = TestData.user();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.deleteUser(id, currentUser)).thenReturn(deletedUser);

        var response = adminController.deleteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).deleteUser(eq(id), eq(currentUser));
    }

    @Test
    public void deleteUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        Long id = currentUser.getDomainUser().getId();
        when(adminService.deleteUser(id, currentUser)).thenThrow(new UsernameNotFoundException("Test message"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminController.deleteUser(id, currentUser, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).deleteUser(eq(id), eq(currentUser));
    }

    @Test
    public void createAdmin_shouldNewAdmin_whenDataValid() {
        RegisterDto newAdmin = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        User user = TestData.admin();
        when(adminService.createAdmin(newAdmin, currentUser)).thenReturn(user);

        var response = adminController.createAdmin(newAdmin, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createAdmin(eq(newAdmin), eq(currentUser));
    }

    @Test
    public void createAdmin_shouldThrowException_whenAdminAlreadyExists() {
        RegisterDto existAdmin = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        when(adminService.createAdmin(existAdmin, currentUser)).thenThrow(new EntityExistsException("Test message"));

        EntityExistsException ex = assertThrows(EntityExistsException.class,
                () -> adminController.createAdmin(existAdmin, currentUser, request));

        assertThat(ex.getMessage()).isNotBlank();
        verify(adminService).createAdmin(eq(existAdmin), eq(currentUser));
    }
}
