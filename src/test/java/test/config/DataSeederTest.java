package test.config;

import com.example.expensetracker.config.DataSeeder;
import com.example.expensetracker.logging.LogEntry;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataSeederTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder encoder;
    
    @InjectMocks
    private DataSeeder dataSeeder;
    
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    private final String ADMIN_EMAIL_SEEDER = "admin@example.com";
    private final String ADMIN_PASSWORD_SEEDER = "admin";
    private final String CREATED_PREFIX = "Администратор создан: почта=";
    private final String EXISTS_PREFIX = "Администратор уже существует";
    
    @BeforeEach
    void setUp() throws Exception {
        System.setOut(new PrintStream(byteArrayOutputStream));
        byteArrayOutputStream.reset();
        
        var email = DataSeeder.class.getDeclaredField("adminEmail");
        email.setAccessible(true);
        email.set(dataSeeder, ADMIN_EMAIL_SEEDER);
        var password = DataSeeder.class.getDeclaredField("adminPassword");
        password.setAccessible(true);
        password.set(dataSeeder, ADMIN_PASSWORD_SEEDER);
    }
    
    @AfterEach
    void cleanUpStreams() {
        System.setOut(originalOut);
    }
    
    @Test
    void run_shouldCreateAdmin_whenNotExist() throws Exception {
        when(userRepository.findByEmail(ADMIN_EMAIL_SEEDER)).thenReturn(Optional.empty());
        when(encoder.encode(ADMIN_PASSWORD_SEEDER)).thenReturn("encoded-password");
        
        dataSeeder.run();
        
        assertThat(byteArrayOutputStream.toString()).startsWith(CREATED_PREFIX);
        verify(userRepository).findByEmail(ADMIN_EMAIL_SEEDER);
        verify(encoder).encode(ADMIN_PASSWORD_SEEDER);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(ADMIN_EMAIL_SEEDER);
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRoles()).containsExactlyInAnyOrder(Role.ADMIN);
        verify(userRepository).save(any());
        verifyNoMoreInteractions(userRepository, encoder);
    }

    @Test
    void run_shouldPrintMessageInConsole_whenAdminAlreadyExist() throws Exception {
        when(userRepository.findByEmail(ADMIN_EMAIL_SEEDER)).thenReturn(Optional.of(new User()));

        dataSeeder.run();

        assertThat(byteArrayOutputStream.toString()).startsWith(EXISTS_PREFIX);
        verify(userRepository).findByEmail(ADMIN_EMAIL_SEEDER);
        verify(encoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
