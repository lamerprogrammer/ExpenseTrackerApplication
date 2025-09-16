package test.security;

import com.example.expensetracker.security.ApiResponseFactory;
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
import static test.util.TestUtils.writeByteToStream;

@ExtendWith(MockitoExtension.class)
public class CustomAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;
    private CustomAccessDeniedHandler handler;
    private ApiResponseFactory factory;
    private Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        factory = mock(ApiResponseFactory.class);
        handler = new CustomAccessDeniedHandler(objectMapper, factory);
    }

    @Test
    public void shouldReturnForbiddenResponse_whenAccessDenied() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(response.getOutputStream()).thenReturn(writeByteToStream(baos));
        when(factory.forbidden(any())).thenReturn(new ApiResponse(
                now,
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Mocked message.",
                "/api/admin/users"));

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        verify(factory).forbidden("/api/admin/users");

        String json = baos.toString();
        ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

        assertThat(apiResponse.getTimestamp()).isEqualTo(now);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(apiResponse.getError()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(apiResponse.getMessage()).isEqualTo("Mocked message.");
        assertThat(apiResponse.getPath()).isEqualTo("/api/admin/users");
    }

    @Test
    public void shouldReturnForbiddenResponse_whenRequestUriIsNull() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn(null);
        when(response.getOutputStream()).thenReturn(writeByteToStream(baos));
        when(factory.forbidden(any())).thenReturn(new ApiResponse(
                now,
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Mocked message.",
                null));

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        verify(factory).forbidden(isNull());

        String json = baos.toString();
        ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

        assertThat(apiResponse.getTimestamp()).isEqualTo(now);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(apiResponse.getError()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(apiResponse.getMessage()).isEqualTo("Mocked message.");
        assertThat(apiResponse.getPath()).isEqualTo(null);
    }
}
