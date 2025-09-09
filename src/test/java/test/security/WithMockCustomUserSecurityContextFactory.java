package test.security;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = User.builder()
                .id(42L)
                .email(customUser.email())
                .password("password")
                .roles(Arrays.stream(customUser.roles()).map(Role::valueOf).collect(Collectors.toSet()))
                .banned(false)
                .build();

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}
