package test.security;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.security.SignatureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static test.util.Constants.USER_EMAIL;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "super-secret-key-which-is-very-long-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 1000L * 60);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 1000L * 120);
    }

    @Test
    public void generateAccessToken_shouldContainEmailAndRole() {
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        Jws<Claims> claimsJws = jwtUtil.parse(token);

        assertThat(claimsJws.getPayload().getSubject()).isEqualTo(USER_EMAIL);
        assertThat(claimsJws.getPayload().get("role")).isEqualTo("USER");
    }

    @Test
    public void generateRefreshToken_shouldContainEmailAndNoRole() {
        String token = jwtUtil.generateRefreshToken(USER_EMAIL);
        Jws<Claims> claimsJws = jwtUtil.parse(token);

        assertThat(claimsJws.getPayload().getSubject()).isEqualTo(USER_EMAIL);
        assertThat(claimsJws.getPayload().get("role")).isNull();
    }

    @Test
    public void getSubject_shouldReturnEmailFromToken() {
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        assertThat(jwtUtil.getSubject(token)).isEqualTo(USER_EMAIL);
    }

    @Test
    public void parse_shouldThrow_whenTokenSignatureInvalid() {
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        String invalidToken = token + "tampered";

        assertThatThrownBy(() -> jwtUtil.parse(invalidToken)).isInstanceOf(SignatureException.class);
    }

    @Test
    public void parse_shouldThrow_whenTokenExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 1L);
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);

        Thread.sleep(2L);

        assertThatThrownBy(() -> jwtUtil.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
