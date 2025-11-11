package test.dto;

import com.example.expensetracker.dto.AdminUserDto;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.example.expensetracker.dto.AdminUserDto.fromEntities;
import static com.example.expensetracker.dto.AdminUserDto.fromEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static test.util.Constants.*;
import static test.util.Constants.USER_EMAIL;

public class AdminUserDtoTest {

    @Test
    void constructor_shouldInitializeAllFieldsCorrectly() {
        AdminUserDto dto = new AdminUserDto(
                ID_VALID,
                USER_EMAIL, 
                Set.of(Role.USER), 
                false,
                false, 
                new BigDecimal(AMOUNT));

        assertThat(dto).extracting(AdminUserDto::getId, AdminUserDto::getEmail, AdminUserDto::isBanned, AdminUserDto::isDeleted)
                .containsExactly(ID_VALID, USER_EMAIL, false, false);
        assertThat(dto.getRoles()).contains(Role.USER);
        assertThat(dto.getTotalExpenses()).isEqualByComparingTo(new BigDecimal(AMOUNT));
    }

    @Test
    void fromEntity_shouldMapAllFieldsCorrectly() {
        User user = TestData.user();

        AdminUserDto dto = fromEntity(user);

        assertThat(dto).extracting(AdminUserDto::getId, AdminUserDto::getEmail, AdminUserDto::isBanned, AdminUserDto::isDeleted)
                .containsExactly(ID_VALID, USER_EMAIL, false, false);
    }

    @Test
    void fromEntities_shouldReturnListOfMappedDtos() {
        User user = TestData.user();
        User admin = TestData.admin();

        List<AdminUserDto> userDto = fromEntities(List.of(user, admin));

        assertThat(userDto)
                .hasSize(2)
                .extracting(AdminUserDto::getId, AdminUserDto::getEmail, AdminUserDto::isBanned, AdminUserDto::isDeleted)
                .containsExactly(
                        tuple(user.getId(), user.getEmail(), user.isBanned(), user.isDeleted()),
                        tuple(admin.getId(), admin.getEmail(), admin.isBanned(), admin.isDeleted()));
        assertThat(userDto.get(0).getRoles()).contains(Role.USER);
        assertThat(userDto.get(1).getRoles()).contains(Role.ADMIN);
    }
}
