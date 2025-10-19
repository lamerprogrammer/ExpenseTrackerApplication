package test.logging.audit;

import com.example.expensetracker.logging.audit.Audit;
import com.example.expensetracker.logging.audit.AuditRepository;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import test.util.TestData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.expensetracker.logging.audit.AuditAction.BAN;
import static com.example.expensetracker.logging.audit.AuditAction.CHANGE_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void logAction_shouldSaveAndReturnDto() {
        User targetUser = TestData.user();
        User admin = TestData.admin();
        Audit audit = new Audit(BAN, targetUser, admin);
        audit.setId(1L);
        audit.setTimeStamp(Instant.now());

        when(auditRepository.save(any(Audit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = auditService.logAction(BAN, targetUser, admin);

        assertThat(result.getAction()).isEqualTo(BAN);
        assertThat(result.getTargetUser()).isEqualTo(USER_EMAIL);
        assertThat(result.getPerformedBy()).isEqualTo(ADMIN_EMAIL);
        verify(auditRepository).save(any(Audit.class));
    }

    @Test
    void getAll_shouldReturnMappedPage() {
        User targetUser = TestData.user();
        User admin = TestData.admin();
        Audit audit = new Audit(BAN, targetUser, admin);
        Page<Audit> audits = new PageImpl<>(List.of(audit));
        when(auditRepository.findAll(any(Pageable.class))).thenReturn(audits);

        var result = auditService.getAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo(BAN);
        verify(auditRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getByAdmin_shouldReturnMappedPage() {
        User targetUser = TestData.user();
        User admin = TestData.admin();
        Audit audit = new Audit(BAN, targetUser, admin);
        Page<Audit> audits = new PageImpl<>(List.of(audit));
        when(auditRepository.findByPerformedBy_Id(eq(ID_ADMIN), any(Pageable.class))).thenReturn(audits);

        var result = auditService.getByAdmin(ID_ADMIN, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo(BAN);
        verify(auditRepository).findByPerformedBy_Id(eq(ID_ADMIN), any(PageRequest.class));
    }

    @Test
    void logPasswordChange_shouldSaveAudit() {
        User user = TestData.user();

        auditService.logPasswordChange(user);
        
        ArgumentCaptor<Audit> auditCaptor = ArgumentCaptor.forClass(Audit.class);
        verify(auditRepository).save(auditCaptor.capture());

        Audit audit = auditCaptor.getValue();

        assertThat(audit.getAction()).isEqualTo(CHANGE_PASSWORD);
        assertThat(audit.getTargetUser()).isEqualTo(user);
        assertThat(audit.getPerformedBy()).isEqualTo(user);
        assertThat(audit.getTimeStamp()).isNotNull();
        assertThat(audit.getTimeStamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}
