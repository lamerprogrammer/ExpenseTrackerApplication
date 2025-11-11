package test.dto;

import com.example.expensetracker.dto.ModeratorUserDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.example.expensetracker.dto.ModeratorUserDto.fromEntities;
import static com.example.expensetracker.dto.ModeratorUserDto.fromEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static test.util.Constants.*;

public class ModeratorUserDtoTest {

    @Test
    void constructor_shouldInitializeAllFieldsCorrectly() {
        ModeratorUserDto dto = new ModeratorUserDto(ID_VALID, USER_EMAIL, Set.of(Role.USER), false, new BigDecimal(AMOUNT));
        
        assertThat(dto).extracting(ModeratorUserDto::getId, ModeratorUserDto::getEmail, ModeratorUserDto::isBanned)
                .containsExactly(ID_VALID, USER_EMAIL, false);
        assertThat(dto.getRoles()).contains(Role.USER);
        assertThat(dto.getTotalExpenses()).isEqualByComparingTo(new BigDecimal(AMOUNT));
    }

    @Test
    void fromEntity_shouldMapAllFieldsCorrectly() {
        User user = TestData.user();

        ModeratorUserDto dto = fromEntity(user);

        assertThat(dto).extracting(ModeratorUserDto::getId, ModeratorUserDto::getEmail, ModeratorUserDto::isBanned)
                .containsExactly(ID_VALID, USER_EMAIL, false);
    }

    @Test
    void fromEntities_shouldReturnListOfMappedDtos() {
        User user = TestData.user();
        User admin = TestData.admin();
        
        List<ModeratorUserDto> userDto = fromEntities(List.of(user, admin));

        assertThat(userDto)
                .hasSize(2)
                .extracting(ModeratorUserDto::getId, ModeratorUserDto::getEmail, ModeratorUserDto::isBanned)
                .containsExactly(
                        tuple(user.getId(), user.getEmail(), user.isBanned()), 
                        tuple(admin.getId(), admin.getEmail(), admin.isBanned()));
        assertThat(userDto.get(0).getRoles()).contains(Role.USER);
        assertThat(userDto.get(1).getRoles()).contains(Role.ADMIN);
    }
}
