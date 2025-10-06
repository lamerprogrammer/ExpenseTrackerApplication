//package test.service;
//
//import com.example.expensetracker.details.UserDetailsImpl;
//import com.example.expensetracker.dto.RegisterDto;
//import com.example.expensetracker.exception.UserNotFoundByIdException;
//import com.example.expensetracker.logging.audit.*;
//import com.example.expensetracker.model.Role;
//import com.example.expensetracker.model.User;
//import com.example.expensetracker.repository.UserRepository;
//import com.example.expensetracker.service.AdminServiceImpl;
//import jakarta.persistence.EntityExistsException;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import test.util.TestData;
//
//import java.awt.print.Pageable;
//import java.util.List;
//import java.util.Optional;
//
//import static com.example.expensetracker.logging.audit.AuditAction.BAN;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.assertj.core.api.AssertionsForClassTypes.tuple;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static test.util.Constants.ID_INVALID;
//
//@ExtendWith(MockitoExtension.class)
//public class AdminServiceImplTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private AuditRepository auditRepository;
//
//    @Mock
//    private Pageable pageable;
//
//    @Mock
//    private AuditService auditService;
//
//    @InjectMocks
//    private AdminServiceImpl adminService;
//
//    @Test
//    public void getAllUsers_shouldReturnListOfUsers() {
//        Page<User> users = new PageImpl<>(List.of(TestData.user()));
//        when(userRepository.findAll()).thenReturn(users);
//
//        var result = adminService.getAllUsers(pageable);
//
//        assertThat(result).isNotNull();
//        assertThat(result).extracting(User::getId, User::getEmail)
//                .containsExactlyInAnyOrder(tuple(users..get(0).getId(), users.get(0).getEmail()));
//        verify(userRepository).findAll();
//    }
//
//    @Test
//    public void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
//        when(userRepository.findAll()).thenReturn(List.of());
//
//        var result = adminService.getAllUsers(pageable);
//
//        assertThat(result).isEmpty();
//        verify(userRepository).findAll();
//    }
//
//    @Test
//    void getUserById_shouldReturnUser_whenUserExists() {
//        User user = TestData.user();
//        Long id = user.getId();
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        var result = adminService.getUserById(id);
//
//        assertThat(result.getEmail()).isEqualTo(user.getEmail());       
//        assertThat(result.getId()).isEqualTo(user.getId());
//        verifyNoMoreInteractions(userRepository);
//    }
//
//    @Test
//    void getUserById_shouldThrowException_whenUserNotExists() {
//        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());
//
//        UserNotFoundByIdException ex = assertThrows(UserNotFoundByIdException.class,
//                () -> adminService.getUserById(ID_INVALID));
//
//        assertThat(ex.getMessage()).isNotBlank();
//        verifyNoMoreInteractions(userRepository);
//    }
//
//    @Test
//    void promoteUser_shouldReturnUser_whenUserExists() {
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//        User user = TestData.user();
//        Long id = user.getId();
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//        when(userRepository.save(user)).thenReturn(Optional.of(user));
//
//        User result = adminService.banUser(id, currentUser);
//
//        assertThat(result).isNotNull();
//        verify(userRepository).save(argThat(User::isBanned));
//        checkLoggerData(BAN, result, currentUser);
//    }
//
//    @Test
//    void promoteUser_shouldThrowException_whenUserNotExists() {
//        User user = TestData.user();
//        Long id = user.getId();
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
//                () -> adminService.getUserById(id));
//
//        assertThat(ex.getMessage()).isNotBlank();
//        verify(userRepository, never()).save(any(User.class));
//        verify(auditRepository, never()).save(any(Audit.class));
//        verifyNoMoreInteractions(userRepository);
//    }
//
//    @Test
//    public void banUser_shouldReturnUser_whenUserExist() {
//        User entity = TestData.admin();
//        UserDetailsImpl currentUser = new UserDetailsImpl(entity);
//        User user = TestData.user();
//        Long id = user.getId();
//        AuditDto auditDto = AuditDto.from(new Audit(BAN, user, entity));
//        when(userRepository.findByEmail(currentUser.getUsername())).thenReturn(Optional.of(entity));
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//        when(auditService.logAction(BAN, user, entity)).thenReturn(auditDto);
//
//        User result = adminService.banUser(id, currentUser);
//
//        assertThat(result).isNotNull();
//        verify(userRepository).save(argThat(User::isBanned));
//        checkLoggerData(BAN, result, currentUser);
//    }
//
//    @Test
//    public void banUser_shouldThrowException_whenUserNotFound() {
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());
//
//        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
//                () -> adminService.banUser(ID_INVALID, currentUser));
//
//        assertThat(ex.getMessage()).isNotBlank();
//        verify(userRepository, never()).save(any(User.class));
//        verify(auditRepository, never()).save(any(Audit.class));
//    }
//
//    @Test
//    public void unbanUser_shouldReturn200_whenUserExist() {
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//        User user = TestData.userBanned();
//        Long id = user.getId();
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        User result = adminService.unbanUser(id, currentUser);
//
//        assertThat(result).isNotNull();
//        verify(userRepository).save(argThat(u -> !u.isBanned()));
//        checkLoggerData(AuditAction.UNBAN, result, currentUser);
//    }
//
//    @Test
//    public void unbanUser_shouldReturn404_whenUserNotFound() {
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());
//
//        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
//                () -> adminService.unbanUser(ID_INVALID, currentUser));
//
//        assertThat(ex.getMessage()).isNotBlank();
//        verify(userRepository, never()).save(any(User.class));
//        verify(auditRepository, never()).save(any(Audit.class));
//        verifyNoMoreInteractions(userRepository);
//    }
//
//    @Test
//    public void deleteUser_shouldDeleteUser_whenUserExist() {
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//        User user = TestData.user();
//        Long id = user.getId();
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        User response = adminService.deleteUser(id, currentUser);
//
//        assertThat(response).isNotNull();
//        verify(userRepository).delete(any(User.class));
//        checkLoggerData(AuditAction.DELETE, response, currentUser);
//    }
//
//    @Test
//    public void deleteUser_shouldReturn404_whenUserNotFound() {
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//        when(userRepository.findById(ID_INVALID)).thenReturn(Optional.empty());
//
//        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
//                () -> adminService.deleteUser(ID_INVALID, currentUser));
//
//        assertThat(ex.getMessage()).isNotBlank();
//        verify(userRepository, never()).delete(any());
//        verify(auditRepository, never()).save(any());
//    }
//
//    @Test
//    public void createAdmin_shouldAssignAdminRole() {
//        RegisterDto dto = TestData.registerDto();
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//
//        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
//        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
//        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        User result = adminService.createAdmin(dto, currentUser);
//
//        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
//        assertThat(result.getPassword()).isEqualTo("encoded");
//        assertThat(result.getRoles()).contains(Role.ADMIN);
//        verify(userRepository).save(any(User.class));
//        checkLoggerData(AuditAction.CREATE, result, currentUser);
//    }
//
//    @Test
//    public void createAdmin_shouldThrowException_whenEmailExists() {
//        RegisterDto dto = TestData.registerDto();
//        UserDetailsImpl currentUser = new UserDetailsImpl(TestData.admin());
//
//        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
//
//        EntityExistsException ex = assertThrows(EntityExistsException.class,
//                () -> adminService.createAdmin(dto, currentUser));
//
//        assertThat(ex.getMessage()).isNotBlank();
//        verify(userRepository, never()).save(any());
//        verify(auditRepository, never()).save(any());
//    }
//
//    private void checkLoggerData(AuditAction action, User targetUser, UserDetailsImpl performedBy) {
//        verify(auditRepository).save(argThat(log ->
//                log.getAction() == action &&
//                        log.getTargetUser().getEmail().equals(targetUser.getEmail()) &&
//                        log.getPerformedBy().getEmail().equals(performedBy.getName())));
//    }
//}
