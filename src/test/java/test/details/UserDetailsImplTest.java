package test.details;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import test.util.TestData;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.USER_PASSWORD;
import static test.util.Constants.USER_EMAIL;

public class UserDetailsImplTest {
    
    @Test
    void getAuthorities_shouldReturnCorrectAuthorities() {
        UserDetailsImpl userDetails = new UserDetailsImpl(TestData.moderator());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_" + Role.MODERATOR.name());
    }

    @Test
    void getAuthorities_shouldReturnEmptyAuthorities_whenRoleIsNull() {
        UserDetailsImpl userDetails = new UserDetailsImpl(new User());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities).isEmpty();
    }
    
    @Test
    void shouldReturnCorrectUsername() {
        UserDetailsImpl userDetails = new UserDetailsImpl(TestData.user());
        
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_EMAIL);
    }

    @Test
    void shouldReturnCorrectPassword() {
        UserDetailsImpl userDetails = new UserDetailsImpl(TestData.user());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getPassword()).isEqualTo(USER_PASSWORD);
    }

    @Test
    void shouldReturnTrueForAllSecurityFlags() {
        UserDetailsImpl userDetails = new UserDetailsImpl(TestData.user());
        
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isAccountNonExpired()).isEqualTo(true);
        assertThat(userDetails.isAccountNonLocked()).isEqualTo(true);
        assertThat(userDetails.isCredentialsNonExpired()).isEqualTo(true);
        assertThat(userDetails.isEnabled()).isEqualTo(true);
    }

    @Test
    void shouldReturnFalse_whenUserIsBlocked() {
        UserDetailsImpl userDetails = new UserDetailsImpl(TestData.userBanned());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isAccountNonLocked()).isEqualTo(false);
        assertThat(userDetails.isEnabled()).isEqualTo(false);
    }
    
    @Test
    void shouldReturnCorrectId() {
        User user = TestData.user();
        
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getDomainUser().getId()).isEqualTo(user.getId());
    }
    
    @Test
    void shouldReturnCorrectUser() {
        User user = TestData.user();

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getDomainUser()).isSameAs(user);
    }

    @Test
    void shouldReturnCorrectName() {
        UserDetailsImpl userDetails = new UserDetailsImpl(TestData.user());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getName()).isEqualTo(USER_EMAIL);
    }
}
