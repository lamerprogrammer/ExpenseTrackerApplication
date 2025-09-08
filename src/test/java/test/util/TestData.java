package test.util;

import com.example.expensetracker.dto.LoginRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;

import java.util.Set;

import static test.util.Constants.*;

public class TestData {

    public static User user(String email, String password, Set<Role> roles, boolean banned) {
        return User.builder()
                .email(email)
                .password(password)
                .roles(roles)
                .banned(banned)
                .build();
    }

    public static User user() {
        return user(USER_EMAIL, PASSWORD, Set.of(Role.USER), false);
    }

    public static User userBanned() {
        return user(USER_EMAIL, PASSWORD, Set.of(Role.USER), true);
    }

    public static User moderator() {
        return user(MODERATOR_EMAIL, PASSWORD, Set.of(Role.MODERATOR), false);
    }

    public static User admin() {
        return user(ADMIN_EMAIL, PASSWORD, Set.of(Role.ADMIN), false);
    }

    public static LoginRequest loginRequest(String mail, String password) {
        return new LoginRequest(mail, password);
    }

    public static LoginRequest loginRequest() {
        return loginRequest(USER_EMAIL, PASSWORD);
    }

    public static RegisterDto registerDto(String user, String mail, String password) {
        return new RegisterDto(user, mail, password);
    }

    public static RegisterDto registerDto() {
        return registerDto(USER_NAME, USER_EMAIL, PASSWORD);
    }
}
