package test.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Полностью очищает все таблицы перед каждым тестом.
 * Работает автоматически для всех тестов с @SpringBootTest.
 */
public class DatabaseCleanupListener implements TestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        JdbcTemplate jdbcTemplate = testContext.getApplicationContext().getBean(JdbcTemplate.class);
        cleanDB(jdbcTemplate);
    }
    
    private void cleanDB(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                DO $$
                    DECLARE
                    r RECORD;
                    BEGIN
                FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                EXECUTE format('TRUNCATE TABLE %I CASCADE', r.tablename);
                END LOOP;
                END; $$
                """);
    }
}
