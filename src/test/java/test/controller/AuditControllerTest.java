//package test.controller;
//
//import com.example.expensetracker.controller.AuditController;
//import com.example.expensetracker.logging.audit.Audit;
//import com.example.expensetracker.logging.audit.AuditRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AuditControllerTest {
//
//    @Mock
//    private AuditRepository auditRepository;
//
//    @InjectMocks
//    private AuditController auditController;
//
//    @Test
//    public void getAllLogs_shouldReturnListOfLogs() {
//        Audit log = new Audit();
//        when(auditRepository.findAll()).thenReturn(List.of(log));
//
//        List<Audit> logs = auditController.getAllAudit();
//
//        assertThat(logs).isNotNull();
//        assertThat(logs).hasSize(1).containsExactly(log);
//        verify(auditRepository, times(1)).findAll();
//    }
//
//    @Test
//    public void getAllLogs_shouldReturnEmptyList_whenNoLogs() {
//        when(auditRepository.findAll()).thenReturn(List.of());
//
//        List<Audit> logs = auditController.getAllLogs();
//
//        assertThat(logs).isEmpty();
//        verify(auditRepository, times(1)).findAll();
//    }
//}
