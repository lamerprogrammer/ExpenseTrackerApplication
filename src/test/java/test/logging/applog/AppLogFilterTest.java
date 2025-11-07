package test.logging.applog;

import com.example.expensetracker.config.RequestLoggingProperties;
import com.example.expensetracker.logging.applog.AppLogDto;
import com.example.expensetracker.logging.applog.AppLogFilter;
import com.example.expensetracker.logging.applog.AppLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;

import static com.example.expensetracker.logging.applog.AppLogLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static test.util.Constants.API_TEST_ENDPOINT;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class AppLogFilterTest {
    
    @Mock
    private AppLogService appLogService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;
    
    @Mock
    private RequestLoggingProperties props;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AppLogFilter appLogFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldLogAnonymousUser() throws Exception {
        Method method = AppLogFilter.class.getDeclaredMethod("doFilterInternal",
                HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);

        when(props.enabled()).thenReturn(true);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);
        when(response.getStatus()).thenReturn(200);

        method.invoke(appLogFilter, request, response, filterChain);

        ArgumentCaptor<AppLogDto> appLogCaptor = ArgumentCaptor.forClass(AppLogDto.class);
        verify(appLogService).log(appLogCaptor.capture());

        AppLogDto dto = appLogCaptor.getValue();

        assertThat(dto).extracting(AppLogDto::getLevel, AppLogDto::getLogger, AppLogDto::getUserEmail,
                        AppLogDto::getEndPoint)
                .containsExactly(INFO, "HTTP", "ANONYMOUS", API_TEST_ENDPOINT);
        assertThat(dto.getMessage()).contains("GET " + API_TEST_ENDPOINT + " -> 200 (").endsWith(" ms)");
    }

    @Test
    void doFilterInternal_shouldLogAuthenticationUser() throws Exception {
        Method method = AppLogFilter.class.getDeclaredMethod("doFilterInternal",
                HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);

        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(USER_EMAIL);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(USER_EMAIL);
        SecurityContextHolder.setContext(context);

        when(props.enabled()).thenReturn(true);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);
        when(response.getStatus()).thenReturn(200);

        method.invoke(appLogFilter, request, response, filterChain);

        ArgumentCaptor<AppLogDto> appLogCaptor = ArgumentCaptor.forClass(AppLogDto.class);
        verify(appLogService).log(appLogCaptor.capture());

        AppLogDto dto = appLogCaptor.getValue();

        assertThat(dto).extracting(AppLogDto::getLevel, AppLogDto::getLogger, AppLogDto::getUserEmail,
                        AppLogDto::getEndPoint)
                .containsExactly(INFO, "HTTP", USER_EMAIL, API_TEST_ENDPOINT);
        assertThat(dto.getMessage()).contains("GET " + API_TEST_ENDPOINT + " -> 200 (").endsWith(" ms)");
    }

    @Test
    void doFilterInternal_shouldLogNotAuthenticationUser() throws Exception {
        Method method = AppLogFilter.class.getDeclaredMethod("doFilterInternal",
                HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        method.setAccessible(true);

        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(props.enabled()).thenReturn(true);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(API_TEST_ENDPOINT);
        when(response.getStatus()).thenReturn(200);

        method.invoke(appLogFilter, request, response, filterChain);

        ArgumentCaptor<AppLogDto> appLogCaptor = ArgumentCaptor.forClass(AppLogDto.class);
        verify(appLogService).log(appLogCaptor.capture());

        AppLogDto dto = appLogCaptor.getValue();

        assertThat(dto).extracting(AppLogDto::getLevel, AppLogDto::getLogger, AppLogDto::getUserEmail,
                        AppLogDto::getEndPoint)
                .containsExactly(INFO, "HTTP", "ANONYMOUS", API_TEST_ENDPOINT);
        assertThat(dto.getMessage()).contains("GET " + API_TEST_ENDPOINT + " -> 200 (").endsWith(" ms)");
    }
}
