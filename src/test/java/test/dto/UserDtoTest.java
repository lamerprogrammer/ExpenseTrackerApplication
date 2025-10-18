package test.dto;

import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.util.List;

import static com.example.expensetracker.dto.UserDto.fromEntities;
import static com.example.expensetracker.dto.UserDto.fromEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static test.util.Constants.*;

public class UserDtoTest {
    
    @Test
    void constructor_shouldReturnCorrectValue() {
        UserDto userDto = new UserDto(ID_VALID, USER_EMAIL);
        
        assertThat(userDto.getId()).isEqualTo(ID_VALID);
        assertThat(userDto.getEmail()).isEqualTo(USER_EMAIL);
    }

    @Test
    void fromEntity_shouldMapAllFieldsCorrectly() {
        User user = TestData.user();
        
        UserDto userDto = fromEntity(user);

        assertThat(userDto).extracting(UserDto::getId, UserDto::getEmail).containsExactly(ID_VALID, USER_EMAIL);
    }

    @Test
    void fromEntities_shouldMapListCorrectly() {
        User user = TestData.user();
        User admin = TestData.user();
        List<UserDto> userDto = fromEntities(List.of(user, admin));

        assertThat(userDto)
                .hasSize(2)
                .extracting(UserDto::getId, UserDto::getEmail)
                .containsExactly(tuple(user.getId(), user.getEmail()), tuple(admin.getId(), admin.getEmail()));
    }
}
