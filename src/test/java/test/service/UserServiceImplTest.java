package test.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static test.util.Constants.USER_PASSWORD;
import static test.util.Constants.USER_PASSWORD_NEW;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUser_shouldReturnUser_whenUserExists() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        String email = details.getUsername();
        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

        var result = userService.getCurrentUser(details);

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmail(eq(email));
    }

    @Test
    void getCurrentUser_shouldThrowException_whenUserNotExists() {
        User user = TestData.user();
        UserDetailsImpl details = new UserDetailsImpl(user);
        String email = details.getUsername();
        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userService.getCurrentUser(details));

        assertThat(ex).isInstanceOf(UsernameNotFoundException.class);
        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository).findByEmail(eq(email));
    }

    @Test
    void changePassword_shouldUpdatePasswordAndLogAction_whenOldPasswordValid() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        ChangePasswordRequest dto = new ChangePasswordRequest(USER_PASSWORD, USER_PASSWORD_NEW);
        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.oldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(dto.newPassword())).thenReturn(USER_PASSWORD_NEW);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(currentUser, dto);

        assertThat(user.getPassword()).isEqualTo(USER_PASSWORD_NEW);
        verify(userRepository).findByEmail(currentUser.getUsername());
        verify(passwordEncoder).matches(dto.oldPassword(), USER_PASSWORD);
        verify(passwordEncoder).encode(dto.newPassword());
        verify(userRepository).save(user);
        verify(auditService).logPasswordChange(user);
    }

    @Test
    void changePassword_shouldThrowException_whenOldPasswordInvalid() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        ChangePasswordRequest dto = new ChangePasswordRequest(USER_PASSWORD, USER_PASSWORD_NEW);
        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.oldPassword(), user.getPassword())).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> userService.changePassword(currentUser, dto));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository).findByEmail(currentUser.getUsername());
        verify(passwordEncoder).matches(dto.oldPassword(), user.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(auditService, never()).logPasswordChange(user);
    }

    @Test
    void changePassword_shouldThrowException_whenUserNotFound() {
        User user = TestData.user();
        UserDetailsImpl currentUser = new UserDetailsImpl(user);
        ChangePasswordRequest dto = new ChangePasswordRequest(USER_PASSWORD, USER_PASSWORD_NEW);
        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userService.changePassword(currentUser, dto));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository).findByEmail(currentUser.getUsername());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(auditService, never()).logPasswordChange(user);
    }

    @Test
    void getTotalExpenses_shouldReturn_whenUserNotFound() {
        User user = TestData.user();
        user.setTotalExpenses(new BigDecimal("500"));
        Long id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var result = userService.getTotalExpenses(id);

        assertThat(result).isEqualByComparingTo("500");
        verify(userRepository).findById(id);
    }

    @Test
    void clearTotalExpensesCache_shouldExecuteWithoutError() {
        userService.clearTotalExpensesCache(1L);
    }
}
