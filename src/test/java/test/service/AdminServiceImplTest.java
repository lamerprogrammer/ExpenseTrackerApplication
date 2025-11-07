package test.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.AdminUserDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.audit.Audit;
import com.example.expensetracker.logging.audit.AuditAction;
import com.example.expensetracker.logging.audit.AuditDto;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.AdminServiceImpl;
import com.example.expensetracker.service.util.UserValidator;
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
import static test.util.Constants.ID_INVALID;
import static test.util.Constants.ID_VALID;

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

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        User user = TestData.user();
        Page<User> users = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        var result = adminService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).extracting(AdminUserDto::getId, AdminUserDto::getEmail)
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
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));

        var result = adminService.getUserById(id);

        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getId()).isEqualTo(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotExists() {
        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

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
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adminService.promoteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u -> u.getRoles().contains(MODERATOR)));
        checkLoggerData(PROMOTE, result, admin);
    }

    @Test
    void promoteUser_shouldReturnUser_whenAlreadyModerator() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User moderator = TestData.moderator();
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(moderator));

        var result = adminService.promoteUser(ID_VALID, currentUser);

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
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

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
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adminService.demoteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u -> !u.getRoles().contains(MODERATOR)));
        checkLoggerData(DEMOTE, result, admin);
    }

    @Test
    void demoteUser_shouldReturnUser_whenAlreadyNotModerator() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));

        var result = adminService.demoteUser(ID_VALID, currentUser);

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
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

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
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.logAction(eq(BAN), eq(user), eq(admin))).thenReturn(auditDto);

        var result = adminService.banUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(User::isBanned));
        checkLoggerData(BAN, result, admin);
    }

    @Test
    void banUser_shouldThrowException_whenIdsMatched() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.banUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void banUser_shouldReturnUser_whenAlreadyBanned() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.userBanned();
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));

        var result = adminService.banUser(ID_VALID, currentUser);

        cacheCheck(result, user, true);
    }

    @Test
    void banUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

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
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adminService.unbanUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        assertThat(result.isBanned()).isFalse();
        verify(userRepository).save(argThat(u -> !u.isBanned()));
        checkLoggerData(UNBAN, result, admin);
    }

    @Test
    void unbanUser_shouldReturnUser_whenAlreadyUnbanned() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        User user = TestData.user();
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));

        var result = adminService.unbanUser(ID_VALID, currentUser);

        cacheCheck(result, user, false);
    }

    @Test
    void unbanUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

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
        when(userValidator.validateAndGetActor(ID_VALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_VALID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adminService.deleteUser(ID_VALID, currentUser);

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(User::isDeleted));
        checkLoggerData(DELETE, result, admin);
    }

    @Test
    void deleteUser_shouldThrowException_whenIdsMatched() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> adminService.deleteUser(ID_INVALID, currentUser));

        assertThat(ex.getMessage()).isNotBlank();
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotExists() {
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userValidator.validateAndGetActor(ID_INVALID, currentUser)).thenReturn(admin);
        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

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
        when(userValidator.getActor(currentUser)).thenReturn(admin);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = adminService.createAdmin(dto, currentUser);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getRoles()).contains(Role.ADMIN);
        verify(userRepository).save(any(User.class));
        checkLoggerData(AuditAction.CREATE, result, admin);//NPE
    }

    @Test
    void createModerator_shouldAssignModeratorRole() {
        RegisterDto dto = TestData.registerDto();
        User admin = TestData.admin();
        UserDetailsImpl currentUser = new UserDetailsImpl(admin);
        when(userValidator.getActor(currentUser)).thenReturn(admin);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = adminService.createModerator(dto, currentUser);

        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getRoles()).contains(MODERATOR);
        verify(userRepository).save(any(User.class));
        checkLoggerData(AuditAction.CREATE, result, admin);
    }

    private void checkLoggerData(AuditAction action, AdminUserDto targetUser, User performedBy) {
        ArgumentCaptor<AuditAction> actionCaptor = ArgumentCaptor.forClass(AuditAction.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> adminCaptor = ArgumentCaptor.forClass(User.class);

        verify(auditService).logAction(actionCaptor.capture(), userCaptor.capture(), adminCaptor.capture());

        var savedUser = userCaptor.getValue();
        var savedAdmin = adminCaptor.getValue();

        assertThat(actionCaptor.getValue()).isEqualTo(action);
        assertThat(savedUser).extracting(User::getId, User::getEmail)
                .containsExactly(targetUser.getId(), targetUser.getEmail());
        assertThat(savedAdmin).extracting(User::getId, User::getEmail)
                .containsExactly(performedBy.getId(), performedBy.getEmail());
    }

    private void cacheCheck(AdminUserDto result, User user, boolean isBanned) {
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.isBanned()).isEqualTo(isBanned);
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logAction(any(), any(), any());
    }
}

