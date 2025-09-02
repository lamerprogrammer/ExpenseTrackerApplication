package com.example.expensetracker;

import com.example.expensetracker.logging.AppLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        properties = {
                "spring.profiles.active=test",
                "spring.liquibase.enabled=false",
                "spring.data.elasticsearch.repositories.enabled=false"
        })
@Import(ExpenseTrackerApplicationTests.TestConfig.class)
class ExpenseTrackerApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        com.example.expensetracker.logging.AppLogRepository appLogRepository() {
            return org.mockito.Mockito.mock(com.example.expensetracker.logging.AppLogRepository.class);
        }
    }

    @Test
    void contextLoads() {
        // если контекст не упал, то тест будет зелёный
    }
}
