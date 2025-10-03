package test.security;


import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.security.CustomAuthEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;

import static com.example.expensetracker.dto.ApiResponseFactory.unauthorized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static test.util.TestUtils.writeByteToStream;

@ExtendWith(MockitoExtension.class)
public class CustomAuthEntryPointTest {

    private ObjectMapper objectMapper;
    private CustomAuthEntryPoint customAuthEntryPoint;
    private Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        customAuthEntryPoint = new CustomAuthEntryPoint(objectMapper);
    }

    @Test
    public void shouldReturnUnauthorizedResponse_whenAuthenticationFails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(response.getOutputStream()).thenReturn(writeByteToStream(baos));
        when(unauthorized(any())).thenReturn(new ApiResponse(
                now,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Mocked message.",
                "/api/auth/login",
                null,
                List.of("Exception")));

        customAuthEntryPoint.commence(request, response,
                new AuthenticationCredentialsNotFoundException("No auth"));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String json = baos.toString();
        ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

        assertThat(apiResponse.getTimestamp()).isEqualTo(now);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(apiResponse.getError()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        assertThat(apiResponse.getMessage()).isEqualTo("Mocked message.");
        assertThat(apiResponse.getPath()).isEqualTo("/api/auth/login");
    }

    @Test
    public void shouldReturnUnauthorizedResponse_whenRequestUriIsNull() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn(null);
        when(response.getOutputStream()).thenReturn(writeByteToStream(baos));
        when(unauthorized(any())).thenReturn(new ApiResponse(
                now,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Mocked message.",
                null,
                null,
                List.of("Exception")));

        customAuthEntryPoint.commence(request, response,
                new AuthenticationCredentialsNotFoundException("No auth"));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        String json = baos.toString();
        ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

        assertThat(apiResponse.getTimestamp()).isEqualTo(now);
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(apiResponse.getError()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        assertThat(apiResponse.getMessage()).isEqualTo("Mocked message.");
        assertThat(apiResponse.getPath()).isEqualTo(null);
    }
}
