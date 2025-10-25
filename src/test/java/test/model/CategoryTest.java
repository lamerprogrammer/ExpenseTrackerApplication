package test.model;

import com.example.expensetracker.model.Category;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static test.util.Constants.CATEGORY_NAME;
import static test.util.Constants.ID_VALID;

public class CategoryTest {
    @Test
    void settersAndGetters() {
        Category category = new Category();

        category.setId(ID_VALID);
        category.setName(CATEGORY_NAME);

        assertThat(category.getId()).isEqualTo(ID_VALID);
        assertThat(category.getName()).isEqualTo(CATEGORY_NAME);
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        Category category = new Category(CATEGORY_NAME);

        assertThat(category.getName()).isEqualTo(CATEGORY_NAME);
    }

    @Test
    void equalsHashCode_contract() {
        EqualsVerifier.forClass(Category.class)
                .usingGetClass()
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void toStringTest() {
        Category category = new Category();
        assertThat(category).asString().contains("id").contains("name");
    }
}
