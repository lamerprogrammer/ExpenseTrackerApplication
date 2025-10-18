package test.util;

public final class Constants {
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String MODERATOR_EMAIL = "moderator@example.com";
    public static final String USER_EMAIL = "user@example.com";
    public static final String NOT_EXISTS_EMAIL = "notfound@example.com";

    public static final String USER_NAME = "User";
    public static final String USER_PASSWORD = "123";

    public static final Long ID_INVALID = 99999L;
    public static final Long ID_VALID = 42L;
    public static final Long ID_ADMIN = 777L;
    public static final String ID_STRING = "42";
    public static final Long ID_EXPENSE = 80L;

    public static final String API_TEST_ENDPOINT = "/api/test";
    public static final String API_ADMIN_USERS_CREATE_MODERATOR = "/api/admin/users/create/moderator";
    public static final String API_ADMIN_USERS_CREATE_ADMINISTRATOR = "/api/admin/users/create/administrator";
    public static final String API_USERS_ME = "/api/users/me";
    public static final String API_USERS_CHANGE_PASSWORD = "/api/users/change-password";
    public static final String API_EXPENSES_REPORT = "/api/expenses/report";
    public static final String API_EXPENSES_TOTAL = "/api/expenses/total";
    
    
    public static final String TYPE_ERROR_WARN = "WARN";
    public static final String TEST_MESSAGE = "Test message";
    public static final String LOGGER_TEST_DATA = "LoggerTestData";
    
    public static final String DESCRIPTION = "description";

    private Constants() {
    }
}
