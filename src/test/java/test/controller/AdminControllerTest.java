package test.controller;

import com.example.expensetracker.controller.AdminController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AdminService;
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
import test.util.TestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
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
        mockMessage();

        var result = adminController.getAllUsers(pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getContent())
                .extracting(UserDto::getId, UserDto::getEmail)
                .containsExactly(tuple(user.getId(), user.getEmail()));
        verify(adminService).getAllUsers(pageable);
        verify(messageSource).getMessage(eq("get.all.users"), any(), any());
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() {
        User user = TestData.user();
        Long id = user.getId();
        when(adminService.getUserById(id)).thenReturn(user);
        mockMessage();

        var result = adminController.getUserById(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(user.getId());
        assertThat(result.getBody().getData().getEmail()).isEqualTo(user.getEmail());
        verify(adminService).getUserById(id);
        verify(messageSource).getMessage(eq("get.user.by.id"), any(), any());
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.banUser(id, currentUser)).thenReturn(bannedUser);
        mockMessage();

        var response = adminController.banUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).banUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("ban.user"), any(), any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.unbanUser(id, currentUser)).thenReturn(bannedUser);
        mockMessage();

        var response = adminController.unbanUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).unbanUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("unban.user"), any(), any());
    }

    @Test
    public void promoteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User user = TestData.user();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.promoteUser(id, currentUser)).thenReturn(user);
        mockMessage();

        var response = adminController.promoteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).promoteUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("promote.user"), any(), any());
    }

    @Test
    public void demoteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User bannedUser = TestData.userBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.demoteUser(id, currentUser)).thenReturn(bannedUser);
        mockMessage();

        var response = adminController.demoteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).demoteUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("demote.user"), any(), any());
    }

    @Test
    public void deleteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        User deletedUser = TestData.user();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.deleteUser(id, currentUser)).thenReturn(deletedUser);
        mockMessage();

        var response = adminController.deleteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).deleteUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("delete.user"), any(), any());
    }

    @Test
    public void createAdmin_shouldNewAdmin_whenDataValid() {
        RegisterDto newAdmin = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        User user = TestData.admin();
        when(adminService.createAdmin(newAdmin, currentUser)).thenReturn(user);
        mockMessage();

        var response = adminController.createAdmin(newAdmin, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createAdmin(eq(newAdmin), eq(currentUser));
        verify(messageSource).getMessage(eq("create.admin"), any(), any());
    }

    @Test
    public void createModer_shouldNewAdmin_whenDataValid() {
        RegisterDto newModer = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        User user = TestData.moderator();
        when(adminService.createModerator(newModer, currentUser)).thenReturn(user);
        mockMessage();

        var response = adminController.createModer(newModer, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createModerator(eq(newModer), eq(currentUser));
        verify(messageSource).getMessage(eq("create.moder"), any(), any());
    }

    private void mockMessage() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }
}
