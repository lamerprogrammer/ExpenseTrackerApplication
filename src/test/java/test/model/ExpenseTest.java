package test.model;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import org.junit.jupiter.api.Test;
import test.util.TestData;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.*;

public class ExpenseTest {

    @Test
    void settersAndGetters() {
        User user = TestData.user();
        BigDecimal amount = new BigDecimal("0");
        Expense expense = new Expense();
        Instant occurredAt = Instant.now();
        Category category = new Category("food");
        
        expense.setId(ID_EXPENSE);
        expense.setUser(user);
        expense.setAmount(amount);
        expense.setOccurredAt(occurredAt);
        expense.setCategory(category);
        expense.setDescription(DESCRIPTION);

        assertThat(expense.getId()).isEqualTo(ID_EXPENSE);
        assertThat(expense.getUser()).satisfies(u -> {
                assertThat(u.getId()).isEqualTo(ID_VALID);
                assertThat(u.getEmail()).isEqualTo(USER_EMAIL);
                assertThat(u.getPassword()).isEqualTo(USER_PASSWORD);
                assertThat(u.isBanned()).isFalse();
                assertThat(u.getRoles()).contains(Role.USER);
        });
        assertThat(expense.getAmount()).isEqualByComparingTo(amount);
        assertThat(expense.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(expense.getCategory().getName()).isEqualTo(category.getName());
        assertThat(expense.getDescription()).isEqualTo(DESCRIPTION);
    }
    
    @Test
    void allArgsConstructor_shouldSetAllFields() {
        User user = TestData.user();
        Category category = new Category("food");
        BigDecimal amount = new BigDecimal("0");
        Instant now = Instant.now();
        
        Expense expense = new Expense(ID_EXPENSE, user, amount, now, category, DESCRIPTION);
        
        assertThat(expense.getId()).isEqualTo(ID_EXPENSE);
        assertThat(expense.getUser()).isEqualTo(user);
        assertThat(expense.getAmount()).isEqualByComparingTo(amount);
        assertThat(expense.getOccurredAt()).isEqualTo(now);
        assertThat(expense.getCategory()).isEqualTo(category);
        assertThat(expense.getDescription()).isEqualTo(DESCRIPTION);
    }
}
