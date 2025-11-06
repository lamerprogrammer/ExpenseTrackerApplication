package test.controller;

import com.example.expensetracker.controller.AuditController;
import com.example.expensetracker.logging.audit.AuditDto;
import com.example.expensetracker.logging.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.mockito.Mockito.*;
import static test.util.Constants.ID_INVALID;
import static test.util.Constants.ID_VALID;

@ExtendWith(MockitoExtension.class)
public class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @Mock
    private Pageable pageable;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AuditController auditController;

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));
    }

    @Test
    public void getAllAudit_shouldReturnListOfLogs() {
        Page<AuditDto> logs = new PageImpl<>(List.of());
        when(auditService.getAll(any())).thenReturn(logs);

        var result = auditController.getAllAudit(pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEqualTo(logs);
        verify(auditService, times(1)).getAll(any());
        verify(messageSource).getMessage(eq("audit.controller.get.all"), isNull(), any());
    }

    @Test
    public void getAllAudit_shouldReturnEmptyList_whenNoLogs() {
        Page<AuditDto> logs = new PageImpl<>(List.of());
        when(auditService.getAll(any())).thenReturn(logs);

        var result = auditController.getAllAudit(pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEmpty();
        verify(auditService, times(1)).getAll(any());
        verify(messageSource).getMessage(eq("audit.controller.get.all"), isNull(), any());
    }

    @Test
    public void getByAdmin_shouldReturnListOfLogs() {
        Page<AuditDto> logs = new PageImpl<>(List.of());
        when(auditService.getByAdmin(anyLong(), any())).thenReturn(logs);

        var result = auditController.getByAdmin(ID_VALID, pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEqualTo(logs);
        verify(auditService, times(1)).getByAdmin(eq(ID_VALID), any());
        verify(messageSource).getMessage(eq("audit.controller.get.by.admin"), isNull(), any());
    }

    @Test
    public void getByAdmin_shouldReturnEmptyList_whenNoLogs() {
        Page<AuditDto> logs = new PageImpl<>(List.of());
        when(auditService.getByAdmin(anyLong(), any())).thenReturn(logs);

        var result = auditController.getByAdmin(ID_INVALID, pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEmpty();
        verify(auditService, times(1)).getByAdmin(eq(ID_INVALID), any());
        verify(messageSource).getMessage(eq("audit.controller.get.by.admin"), isNull(), any());
    }
}

