package com.example.expensetracker;

import com.example.expensetracker.config.TestBeansConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.profiles.active=test",
                "spring.liquibase.enabled=false",
                "spring.data.elasticsearch.repositories.enabled=false"
        },
        classes = TestBeansConfig.class)
class ExpenseTrackerApplicationTests {

    @Test
    void contextLoads() {
        // если контекст не упал, то тест будет зелёный
    }
}
