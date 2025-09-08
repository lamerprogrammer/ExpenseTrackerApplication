package test.controller;

import com.example.expensetracker.controller.AuditController;
import com.example.expensetracker.model.AuditLog;
import com.example.expensetracker.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditControllerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditController auditController;

    @Test
    public void getAllLogs_shouldReturnListOfLogs() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.findAll()).thenReturn(List.of(log));

        List<AuditLog> logs = auditController.getAllLogs();

        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(1).containsExactly(log);
        verify(auditLogRepository, times(1)).findAll();
    }

    @Test
    public void getAllLogs_shouldReturnEmptyList_whenNoLogs() {
        when(auditLogRepository.findAll()).thenReturn(List.of());

        List<AuditLog> logs = auditController.getAllLogs();

        assertThat(logs).isEmpty();
        verify(auditLogRepository, times(1)).findAll();
    }
}
