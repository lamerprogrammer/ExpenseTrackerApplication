//package test.logging.audit;
//
//import com.example.expensetracker.logging.audit.Audit;
//import com.example.expensetracker.logging.audit.AuditRepository;
//import com.example.expensetracker.logging.audit.AuditService;
//import com.example.expensetracker.model.User;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import test.util.TestData;
//
//import java.awt.print.Pageable;
//
//import static com.example.expensetracker.logging.audit.AuditAction.BAN;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class AuditServiceTest {
//    
//    @Mock
//    private AuditRepository auditRepository;
//    
//    @Mock
//    private Pageable pageable;
//    
//    @InjectMocks
//    private AuditService auditService;
//    
//    @Test
//    void logAction() {
//        User user = TestData.user();
//        User admin = TestData.user();
//        Audit audit = new Audit(BAN, user, admin);
//        when(auditRepository.save(audit)).thenReturn()
//        
//        var result = auditService.logAction(BAN, TestData.user(), TestData.admin());
//        
//        assertThat(result.getAction()).isEqualTo(BAN);
//        assertThat(result.getTargetUser()).isEqualTo(user);
//        assertThat(result.getPerformedBy()).isEqualTo(admin);
//    }
//
//    @Test
//    void getAll() {
//        when(auditRepository.findAll(pageable)).thenReturn()
//
//        var result = auditService.getAll(pageable);
//
//        assertThat(result.getAction()).isEqualTo(BAN);
//        assertThat(result.getTargetUser()).isEqualTo(user);
//        assertThat(result.getPerformedBy()).isEqualTo(admin);
//    }
//
//    @Test
//    void getByAdmin() {
//
//    }
//}
