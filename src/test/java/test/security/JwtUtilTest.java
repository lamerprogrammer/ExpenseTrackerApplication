package test.security;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
    void generateAccessToken_shouldContainEmailAndRole() {
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        Jws<Claims> claimsJws = jwtUtil.parse(token);

        assertThat(claimsJws.getPayload().getSubject()).isEqualTo(USER_EMAIL);
        assertThat(claimsJws.getPayload().get("role")).isEqualTo("USER");
        assertThat(claimsJws.getPayload().get("jti")).isNotNull();
    }

    @Test
    void generateRefreshToken_shouldContainEmailAndNoRole() {
        String token = jwtUtil.generateRefreshToken(USER_EMAIL);
        Jws<Claims> claimsJws = jwtUtil.parse(token);

        assertThat(claimsJws.getPayload().getSubject()).isEqualTo(USER_EMAIL);
        assertThat(claimsJws.getPayload().get("role")).isNull();
        assertThat(claimsJws.getPayload().get("jti")).isNotNull();
    }

    @Test
    void getSubject_shouldReturnEmailFromToken() {
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        assertThat(jwtUtil.getSubject(token)).isEqualTo(USER_EMAIL);
    }

    @Test
    void parse_shouldThrow_whenTokenSignatureInvalid() {
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        String invalidToken = token + "tampered";

        assertThatThrownBy(() -> jwtUtil.parse(invalidToken)).isInstanceOf(SignatureException.class);
    }

    @Test
    void parse_shouldThrow_whenTokenExpired() {
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", -1L);
        String token = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);

        assertThatThrownBy(() -> jwtUtil.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void generateAccessToken_shouldProduceDifferentTokens() {
        String accessToken1 = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);
        String accessToken2 = jwtUtil.generateAccessToken(USER_EMAIL, Role.USER);

        assertThat(accessToken1).isNotEqualTo(accessToken2);
    }
}
