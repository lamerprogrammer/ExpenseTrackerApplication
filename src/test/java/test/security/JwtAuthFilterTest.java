package test.security;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.model.User;
import com.example.expensetracker.security.CustomUserDetailsService;
import com.example.expensetracker.security.JwtAuthFilter;
import com.example.expensetracker.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import test.util.TestData;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static test.util.Constants.API_USERS_ME;
import static test.util.Constants.USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private Method doFilterInternal;
    private Method resolveToken;

    @BeforeEach
    void setUp() throws Exception {
        doFilterInternal = JwtAuthFilter.class.getDeclaredMethod("doFilterInternal",
                HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
        doFilterInternal.setAccessible(true);

        resolveToken = JwtAuthFilter.class.getDeclaredMethod("resolveToken",
                HttpServletRequest.class);
        resolveToken.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String callResolveToken(HttpServletRequest request) throws Exception {
        return (String) resolveToken.invoke(jwtAuthFilter, request);
    }

    @Test
    void resolveToken_shouldReturnToken_whenHeaderIsCorrect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        String result = callResolveToken(request);

        assertThat(result).isEqualTo("token");
    }

    @Test
    void resolveToken_shouldReturnNull_whenHeaderIsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = callResolveToken(request);

        assertThat(result).isNull();
    }

    @Test
    void resolveToken_shouldReturnNull_whenHeaderStartsWithNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "token");

        String result = callResolveToken(request);

        assertThat(result).isNull();
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenValidBearerTokenAndNoAuthInContext() throws Exception {
        String token = "valid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        User user = TestData.user();
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(jwtUtil.getSubject(token)).thenReturn(USER_EMAIL);

        doFilterInternal.invoke(jwtAuthFilter, request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getName()).isEqualTo(USER_EMAIL);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldSkipAuthentication_whenPathApiAuth() throws Exception {
        checkPath("/api/auth");
        verifyNoMoreInteractions(jwtUtil, userDetailsService);
    }
    
    @Test
    void doFilterInternal_shouldSkipAuthentication_whenPathSwaggerUi() throws Exception {
        checkPath("/swagger-ui");
        verifyNoMoreInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_shouldSkipAuthentication_whenPathV3ApiDocs() throws Exception {
        checkPath("/v3/api-docs");
        verifyNoMoreInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_shouldSkipAuthentication_whenPathWebjars() throws Exception {
        checkPath("/webjars");
        verifyNoMoreInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenValidTokenProvided() throws Exception {
        String token = "valid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        User user = TestData.user();
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(jwtUtil.getSubject(token)).thenReturn(USER_EMAIL);

        doFilterInternal.invoke(jwtAuthFilter, request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getName()).isEqualTo(USER_EMAIL);
        verify(filterChain).doFilter(request, response);
    }

    @Test void doFilterInternal_shouldResponseUnauthorized_whenTokenNotPresentAndContextEmpty() throws Exception { 
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(API_USERS_ME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
        
        doFilterInternal.invoke(jwtAuthFilter, request, response, filterChain);
        
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        verify(jwtUtil, never()).getSubject(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldDoNothing_whenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        doFilterInternal.invoke(jwtAuthFilter, request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).parse(anyString());
        verify(filterChain, never()).doFilter(eq(request), eq(response));
    }

    @Test
    void doFilterInternal_shouldLogWarningAndContinue_whenJwtThrowsException() throws Exception {
        String token = "invalid.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.getSubject(token)).thenThrow(new RuntimeException("JWT is invalid"));

        doFilterInternal.invoke(jwtAuthFilter, request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private void checkPath(String requestURI) throws IllegalAccessException, InvocationTargetException, IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRequestURI(requestURI);

        doFilterInternal.invoke(jwtAuthFilter, request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
