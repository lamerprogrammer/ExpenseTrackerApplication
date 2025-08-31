package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.security.CustomAccessDeniedHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static test.util.UtilForTests.writeByteToStream;

@ExtendWith(MockitoExtension.class)
public class CustomAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;
    private CustomAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new CustomAccessDeniedHandler(objectMapper);
    }

    @Test
    public void shouldReturnApiResponseWith403Status() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(response.getOutputStream()).thenReturn(writeByteToStream(baos));

        Instant before = Instant.now();
        handler.handle(request, response, new AccessDeniedException("Forbidden"));
        Instant after = Instant.now();

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");

        String json = baos.toString();
        ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

        assertThat(apiResponse.getTimestamp()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(apiResponse.getError()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(apiResponse.getMessage()).isEqualTo("У вас недостаточно прав для доступа к этому ресурсу.");
        assertThat(apiResponse.getPath()).isEqualTo("/api/admin/users");
    }

    @Test
    public void shouldReturnApiResponseWith403Status_whenURIIsNull() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn(null);
        when(response.getOutputStream()).thenReturn(writeByteToStream(baos));

        Instant before = Instant.now();
        handler.handle(request, response, new AccessDeniedException("Forbidden"));
        Instant after = Instant.now();

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");

        String json = baos.toString();
        ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

        assertThat(apiResponse.getTimestamp()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(apiResponse.getError()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(apiResponse.getMessage()).isEqualTo("У вас недостаточно прав для доступа к этому ресурсу.");
        assertThat(apiResponse.getPath()).isEqualTo(null);
    }
}
