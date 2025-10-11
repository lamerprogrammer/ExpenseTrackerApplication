package test.logging.applog;

import com.example.expensetracker.logging.applog.AppLog;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogRepository;
import com.example.expensetracker.logging.applog.AppLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import test.util.TestData;

import java.util.List;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.util.Constants.*;

@ExtendWith(MockitoExtension.class)
public class AppLogServiceTest {

    @Mock
    private AppLogRepository appLogRepository;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private AppLogService appLogService;

    @Test
    void log_shouldSaveEntity() {
        AppLog appLog = TestData.appLog();
        AppLogDto appLogDto = AppLogDto.from(appLog);
        when(appLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppLogDto result = appLogService.log(appLogDto);

        assertThat(result).extracting(AppLogDto::getId, AppLogDto::getLevel, AppLogDto::getLogger,
                        AppLogDto::getMessage, AppLogDto::getUserEmail, AppLogDto::getEndPoint, AppLogDto::getErrorType)
                .containsExactly(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN);
        verify(appLogRepository).save(any());
    }

    @Test
    void findAll_shouldMapCorrectAndReturnPage() {
        AppLog appLog = TestData.appLog();
        Page<AppLog> appLogs = new PageImpl<>(List.of(appLog));
        when(appLogRepository.findAll(any(Pageable.class))).thenReturn(appLogs);

        var result = appLogService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).extracting(AppLogDto::getId, AppLogDto::getLevel, AppLogDto::getLogger,
                        AppLogDto::getMessage, AppLogDto::getUserEmail, AppLogDto::getEndPoint, AppLogDto::getErrorType)
                .containsExactly(tuple(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN));
        verify(appLogRepository).findAll(any(Pageable.class));
    }

    @Test
    void findAll_shouldReturnEmptyPage() {
        when(appLogRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        var result = appLogService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        verify(appLogRepository).findAll(any(Pageable.class));
    }

    @Test
    void findByUserEmail_shouldMapCorrectAndReturnPage() {
        AppLog appLog = TestData.appLog();
        Page<AppLog> appLogs = new PageImpl<>(List.of(appLog));
        when(appLogRepository.findByUserEmail(eq(USER_EMAIL), any(Pageable.class))).thenReturn(appLogs);

        var result = appLogService.findByUserEmail(USER_EMAIL, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).extracting(AppLogDto::getId, AppLogDto::getLevel, AppLogDto::getLogger,
                        AppLogDto::getMessage, AppLogDto::getUserEmail, AppLogDto::getEndPoint, AppLogDto::getErrorType)
                .containsExactly(tuple(ID_STRING, INFO, LOGGER_TEST_DATA, TEST_MESSAGE, USER_EMAIL, API_TEST_ENDPOINT,
                        TYPE_ERROR_WARN));
        verify(appLogRepository).findByUserEmail(eq(USER_EMAIL), any(Pageable.class));
    }

    @Test
    void findByUserEmail_shouldReturnEmptyPage() {
        when(appLogRepository.findByUserEmail(eq(USER_EMAIL), any(Pageable.class))).thenReturn(Page.empty());

        var result = appLogService.findByUserEmail(USER_EMAIL, pageable);

        assertThat(result.getContent()).isEmpty();
        verify(appLogRepository).findByUserEmail(eq(USER_EMAIL), any(Pageable.class));
    }
}
