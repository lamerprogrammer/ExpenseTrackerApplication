//package test.controller;
//
//import com.example.expensetracker.controller.AppLogController;
//import com.example.expensetracker.logging.applog.AppLogDto;
//import com.example.expensetracker.logging.applog.AppLogService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import org.springframework.data.domain.Pageable;
//import test.util.TestData;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static test.util.Constants.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AppLogDtoControllerTest {
//
//    @Mock
//    private AppLogService appLogService;
//    
//    @Mock
//    private HttpServletRequest request;
//    
//    @Mock
//    private Pageable pageable;
//
//    @InjectMocks
//    private AppLogController appLogController;
//
//    @Test
//    public void getAllLogs_shouldReturnLogs_whenLogsExist() {
//        List<AppLogDto> logs = List.of(TestData.appLog());
//        when(appLogService.findAllPaged()).thenReturn(logs);
//
//        var result = appLogController.getAllLogs(pageable, request);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isEqualTo(logs);
//        verify(appLogService).findAllPaged();
//    }
//
//    @Test
//    public void getAll_shouldReturnEmptyList_whenNoLogs() {
//        when(appLogService.findAllPaged()).thenReturn(List.of());
//
//        var result = appLogController.getAll();
//
//        assertThat(result).isEmpty();
//        verify(appLogService).findAllPaged();
//    }
//
//    @Test
//    public void getByUser_shouldReturnLogs_whenLogsExist() {
//        List<AppLogDto> logs = List.of(TestData.appLog());
//        when(appLogService.findByUserEmail(USER_EMAIL)).thenReturn(logs);
//
//        var result = appLogController.getByUser(USER_EMAIL, request);
//
//        assertThat(result.getStatusCode()).isEqualTo(200);
//        assertThat(result).isEqualTo(logs);
//        verify(appLogService).findByUserEmail(USER_EMAIL);
//    }
//
//    @Test
//    public void getByUser_shouldReturnEmptyList_whenNoLogs() {
//        when(appLogService.findByUserEmail(USER_EMAIL)).thenReturn(List.of());
//
//        var result = appLogController.getByUser(USER_EMAIL);
//
//        assertThat(result).isEmpty();
//        verify(appLogService).findByUserEmail(USER_EMAIL);
//    }
//}
