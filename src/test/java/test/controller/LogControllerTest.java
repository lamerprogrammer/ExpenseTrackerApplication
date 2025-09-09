package test.controller;

import com.example.expensetracker.controller.LogController;
import com.example.expensetracker.logging.AppLog;
import com.example.expensetracker.logging.LogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import test.util.TestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class LogControllerTest {

    @Mock
    private LogService logService;

    @InjectMocks
    LogController logController;

    @Test
    public void getAll_shouldReturnLogs_whenLogsExist() {
        List<AppLog> logs = List.of(TestData.appLog());
        when(logService.findAll()).thenReturn(logs);

        List<AppLog> result = logController.getAll();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(logs);
        verify(logService).findAll();
    }

    @Test
    public void getAll_shouldReturnEmptyList_whenNoLogs() {
        when(logService.findAll()).thenReturn(List.of());

        List<AppLog> result = logController.getAll();

        assertThat(result).isEmpty();
        verify(logService).findAll();
    }

    @Test
    public void getByUser_shouldReturnLogs_whenLogsExist() {
        List<AppLog> logs = List.of(TestData.appLog());
        when(logService.findByUserEmail(USER_EMAIL)).thenReturn(logs);

        List<AppLog> result = logController.getByUser(USER_EMAIL);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(logs);
        verify(logService).findByUserEmail(USER_EMAIL);
    }

    @Test
    public void getByUser_shouldReturnEmptyList_whenNoLogs() {
        when(logService.findByUserEmail(USER_EMAIL)).thenReturn(List.of());

        List<AppLog> result = logController.getByUser(USER_EMAIL);

        assertThat(result).isEmpty();
        verify(logService).findByUserEmail(USER_EMAIL);
    }
}
