package test.controller;

import com.example.expensetracker.controller.ModeratorController;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ModeratorUserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.ModeratorService;
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
public class ModeratorControllerTest {

    @Mock
    private ModeratorService moderatorService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private ModeratorController moderatorController;

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(moderatorService);
    }

    @Test
    public void getAllUsers_shouldReturnListOfUsers() {
        ModeratorUserDto user = TestData.moderUserDto();
        Page<ModeratorUserDto> users = new PageImpl<>(List.of(user));
        when(moderatorService.getAllUsers(pageable)).thenReturn(users);

        var result = moderatorController.getAllUsers(pageable, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getContent()).isNotNull()
                .extracting(ModeratorUserDto::getId, ModeratorUserDto::getEmail)
                .containsExactly(tuple(user.getId(), user.getEmail()));
        verify(moderatorService).getAllUsers(pageable);
        verify(messageSource).getMessage(eq("get.all.users"), isNull(), any());
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() {
        ModeratorUserDto user = TestData.moderUserDto();
        Long id = user.getId();
        when(moderatorService.getUserById(id)).thenReturn(user);

        var result = moderatorController.getUserById(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getId()).isEqualTo(user.getId());
        assertThat(body.getData().getEmail()).isEqualTo(user.getEmail());
        verify(moderatorService).getUserById(id);
        verify(messageSource).getMessage(eq("get.user.by.id"), isNull(), any());
    }

    @Test
    public void banUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        ModeratorUserDto bannedUser = TestData.moderUserDtoBanned();
        Long id = currentUser.getDomainUser().getId();
        when(moderatorService.banUser(id, currentUser)).thenReturn(bannedUser);

        var response = moderatorController.banUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(moderatorService).banUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("ban.user"), isNull(), any());
    }

    @Test
    public void unbanUser_shouldReturn200_whenUserExist() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.user());
        ModeratorUserDto bannedUser = TestData.moderUserDtoBanned();
        Long id = currentUser.getDomainUser().getId();
        when(moderatorService.unbanUser(id, currentUser)).thenReturn(bannedUser);

        var response = moderatorController.unbanUser(id, currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(moderatorService).unbanUser(eq(id), eq(currentUser));
        verify(messageSource).getMessage(eq("unban.user"), isNull(), any());
    }
}

