package test.controller;

import com.example.expensetracker.controller.AppLogController;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import test.util.TestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class AppLogControllerTest {

    @Mock
    private AppLogService appLogService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Pageable pageable;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AppLogController appLogController;

    @Test
    public void getAllLogs_shouldReturnLogs_whenLogsExist() {
        Page<AppLogDto> logs = TestData.appLogDtoPage();
        when(appLogService.findAll(pageable)).thenReturn(logs);

        var result = appLogController.getAllLogs(pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEqualTo(logs);
        verify(appLogService).findAll(any());
    }

    @Test
    public void getByUser_shouldReturnLogs_whenLogsExist() {
        Page<AppLogDto> logs = TestData.appLogDtoPage();
        when(appLogService.findByUserEmail(USER_EMAIL, pageable)).thenReturn(logs);

        var result = appLogController.getByUser(USER_EMAIL, pageable, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEqualTo(logs);
        verify(appLogService).findByUserEmail(eq(USER_EMAIL), any());
    }
}
