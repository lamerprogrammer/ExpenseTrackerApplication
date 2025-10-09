package test.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.audit.*;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.AdminServiceImpl;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import test.util.TestData;

import java.util.List;
import java.util.Optional;

import static com.example.expensetracker.logging.audit.AuditAction.*;
import static com.example.expensetracker.model.Role.MODERATOR;
import static com.example.expensetracker.model.Role.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Pageable pageable;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        User user = TestData.user();
        Page<User> users = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        var result = adminService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).extracting(User::getId, User::getEmail)
                .containsExactly(user.getId(), user.getEmail());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        var result = adminService.getAllUsers(pageable);

        assertThat(result).isEmpty();
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        User user = TestData.user();
        Long id = user.getId();
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));

        var result = adminService.getUserById(id);

        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getId()).isEqualTo(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotExists() {
        when(userRepository.findById(eq(ID_INVALID))).thenReturn(Optional.empty());

        UserNotFoundByIdException ex = assertThrows(UserNotFoundByIdException.class,
                () -> adminService.getUserById(ID_INVALID));

        assertThat(ex.getMessage()).isNotBlank();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void promoteUser_shouldReturnUser_whenUserExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = adminService.promoteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u -> u.getRoles().contains(MODERATOR)));
        checkLoggerData(PROMOTE, result, admin);
    }

    @Test
    void promoteUser_shouldReturnUser_whenAlreadyModerator() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User moderator = TestData.moderator();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(moderator));

        User result = adminService.promoteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(moderator.getId());
        assertThat(result.getEmail()).isEqualTo(moderator.getEmail());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(USER, MODERATOR);
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void promoteUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_INVALID))).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.promoteUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void demoteUser_shouldReturnUser_whenUserExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.moderator();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = adminService.demoteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u -> !u.getRoles().contains(MODERATOR)));
        checkLoggerData(DEMOTE, result, admin);
    }

    @Test
    void demoteUser_shouldReturnUser_whenAlreadyNotModerator() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));

        User result = adminService.demoteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getRoles()).containsExactly(USER);
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void demoteUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_INVALID))).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.demoteUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void banUser_shouldReturnUser_whenUserExist() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        AuditDto auditDto = AuditDto.from(new Audit(BAN, user, admin));
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.logAction(eq(BAN), eq(user), eq(admin))).thenReturn(auditDto);

        User result = adminService.banUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(User::isBanned));
        checkLoggerData(BAN, result, admin);
    }

    @Test
    void banUser_shouldThrowException_whenIdsMatched() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.banUser(admin.getId(), currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void banUser_shouldReturnUser_whenAlreadyBanned() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.userBanned();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));

        User result = adminService.banUser(ID_VALID, currentUser);

        cacheCheck(result, user, true);
    }

    @Test
    void banUser_shouldThrowException_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.banUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void banUser_shouldThrowException_whenAdminNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenThrow(new UsernameNotFoundException("message"));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.banUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void banUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_INVALID))).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.banUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void unbanUser_shouldReturn200_whenUserExist() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.userBanned();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = adminService.unbanUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        assertThat(result.isBanned()).isFalse();
        verify(userRepository).save(argThat(u -> !u.isBanned()));
        checkLoggerData(UNBAN, result, admin);
    }

    @Test
    void unbanUser_shouldReturnUser_whenAlreadyNotUnbanned() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));

        User result = adminService.unbanUser(ID_VALID, currentUser);

        cacheCheck(result, user, false);
    }

    @Test
    void unbanUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.unbanUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void unbanUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_INVALID))).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.unbanUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void deleteUser_shouldDeleteUser_whenUserExist() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_VALID))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = adminService.deleteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(User::isDeleted));
        checkLoggerData(DELETE, result, admin);
    }

    @Test
    void deleteUser_shouldThrowException_whenIdsMatched() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.deleteUser(admin.getId(), currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() {
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.deleteUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).delete(any());
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.findById(eq(ID_INVALID))).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.deleteUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void createAdmin_shouldAssignAdminRole() {
        RegisterDto dto = TestData.registerDto();
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adminService.createAdmin(dto, currentUser);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRoles()).contains(Role.ADMIN);
        verify(userRepository).save(any(User.class));
        checkLoggerData(AuditAction.CREATE, result, admin);
    }

    @Test
    void createAdmin_shouldThrowException_whenAdminNotFound() {
        RegisterDto dto = TestData.registerDto();
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.createAdmin(dto, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void createAdmin_shouldThrowException_whenEmailExists() {
        RegisterDto dto = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        User admin = currentUser.getDomainUser();

        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        EntityExistsException ex = assertThrows(EntityExistsException.class,
                () -> adminService.createAdmin(dto, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any());
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void createModerator_shouldAssignModeratorRole() {
        RegisterDto dto = TestData.registerDto();
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));

        User result = adminService.createModerator(dto, currentUser);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRoles()).contains(MODERATOR);
        verify(userRepository).save(any(User.class));
        checkLoggerData(AuditAction.CREATE, result, admin);
    }

    @Test
    void createModerator_shouldThrowException_whenAdminNotFound() {
        RegisterDto dto = TestData.registerDto();
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> adminService.createModerator(dto, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
     void createModerator_shouldThrowException_whenEmailExists() {
        RegisterDto dto = TestData.registerDto();
        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
        User admin = currentUser.getDomainUser();

        when(userRepository.findByEmail(eq(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        EntityExistsException ex = assertThrows(EntityExistsException.class,
                () -> adminService.createModerator(dto, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any());
        verify(auditService, never()).logAction(any(), any(), any());
    }

    private void checkLoggerData(AuditAction action, User targetUser, User performedBy) {
        ArgumentCaptor<AuditAction> actionCaptor = ArgumentCaptor.forClass(AuditAction.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> adminCaptor = ArgumentCaptor.forClass(User.class);
        
        verify(auditService).logAction(actionCaptor.capture(), userCaptor.capture(), adminCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        User savedAdmin = adminCaptor.getValue();

        assertThat(actionCaptor.getValue()).isEqualTo(action);
        assertThat(savedUser.getId()).isEqualTo(targetUser.getId());
        assertThat(savedAdmin.getId()).isEqualTo(performedBy.getId());
        assertThat(savedUser.getEmail()).isEqualTo(targetUser.getEmail());
        assertThat(savedAdmin.getEmail()).isEqualTo(performedBy.getEmail());
    }

    private void cacheCheck(User result, User user, boolean isBanned) {
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.isBanned()).isEqualTo(isBanned);
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }
}
