package test.dto;

import com.example.expensetracker.dto.AuthResponse;
import com.example.expensetracker.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.USER_EMAIL;

public class AuthResponseTest {

    @Test
    void shouldPass_whenAllFieldsValid() {
        AuthResponse auth = new AuthResponse("token", USER_EMAIL, Role.USER.name());

        assertThat(auth.getToken()).isEqualTo("token");
        assertThat(auth.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(auth.getRole()).isEqualTo(Role.USER.name());
    }
}
