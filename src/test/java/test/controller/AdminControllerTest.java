package test.controller;

import com.example.expensetracker.controller.AdminController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.AdminUserDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(adminService);
    }

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        AdminUserDto dto = TestData.adminUserDto();
        Page<AdminUserDto> users = new PageImpl<>(List.of(dto));
        when(adminService.getAllUsers(pageable)).thenReturn(users);

        var result = adminController.getAllUsers(pageable, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getContent())
                .extracting(AdminUserDto::getId, AdminUserDto::getEmail)
                .containsExactly(tuple(dto.getId(), dto.getEmail()));
        verify(adminService).getAllUsers(pageable);
        verify(messageSource).getMessage(eq("get.all.users"), isNull(), any());
    }

    @Test
    public void getAllUsers_shouldReturnEmptyPage() {
        when(adminService.getAllUsers(pageable)).thenReturn(Page.empty());

        var result = adminController.getAllUsers(pageable, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getContent()).isEmpty();
        verify(adminService).getAllUsers(pageable);
        verify(messageSource).getMessage(eq("get.all.users"), isNull(), any());
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() {
        AdminUserDto dto = TestData.adminUserDto();
        Long id = dto.getId();
        when(adminService.getUserById(id)).thenReturn(dto);

        var result = adminController.getUserById(id, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getId()).isEqualTo(dto.getId());
        assertThat(body.getData().getEmail()).isEqualTo(dto.getEmail());
        verify(adminService).getUserById(id);
        verify(messageSource).getMessage(eq("get.user.by.id"), isNull(), any());
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        AdminUserDto banned = TestData.adminUserDtoBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.banUser(id, currentUser)).thenReturn(banned);

        var response = adminController.banUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).banUser(id, currentUser);
        verify(messageSource).getMessage(eq("ban.user"), isNull(), any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        AdminUserDto banned = TestData.adminUserDtoBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.unbanUser(id, currentUser)).thenReturn(banned);

        var response = adminController.unbanUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).unbanUser(id, currentUser);
        verify(messageSource).getMessage(eq("unban.user"), isNull(), any());
    }

    @Test
    public void promoteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        AdminUserDto dto = TestData.adminUserDto();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.promoteUser(id, currentUser)).thenReturn(dto);


        var response = adminController.promoteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).promoteUser(id, currentUser);
        verify(messageSource).getMessage(eq("promote.user"), isNull(), any());
    }

    @Test
    public void demoteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        AdminUserDto banned = TestData.adminUserDtoBanned();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.demoteUser(id, currentUser)).thenReturn(banned);

        var response = adminController.demoteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).demoteUser(id, currentUser);
        verify(messageSource).getMessage(eq("demote.user"), isNull(), any());
    }

    @Test
    public void deleteUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        AdminUserDto deleted = TestData.adminUserDtoDeleted();
        Long id = currentUser.getDomainUser().getId();
        when(adminService.deleteUser(id, currentUser)).thenReturn(deleted);

        var response = adminController.deleteUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).deleteUser(id, currentUser);
        verify(messageSource).getMessage(eq("delete.user"), isNull(), any());
    }

    @Test
    public void createAdmin_shouldReturnNewAdmin_whenDataValid() {
        RegisterDto newAdmin = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        AdminUserDto admin = TestData.adminUserDtoRoleAdmin();
        when(adminService.createAdmin(newAdmin, currentUser)).thenReturn(admin);

        var response = adminController.createAdmin(newAdmin, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createAdmin(eq(newAdmin), eq(currentUser));
        verify(messageSource).getMessage(eq("create.admin"), isNull(), any());
    }

    @Test
    public void createModer_shouldReturnNewModer_whenDataValid() {
        RegisterDto newModer = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        AdminUserDto moderator = TestData.adminUserDtoRoleModerator();
        when(adminService.createModerator(newModer, currentUser)).thenReturn(moderator);

        var response = adminController.createModer(newModer, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminService).createModerator(eq(newModer), eq(currentUser));
        verify(messageSource).getMessage(eq("create.moder"), isNull(), any());
    }
}

