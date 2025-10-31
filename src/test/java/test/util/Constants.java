package test.util;

public final class Constants {
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String MODERATOR_EMAIL = "moderator@example.com";
    public static final String USER_EMAIL = "user@example.com";

    public static final String USER_NAME = "User";
    public static final String USER_PASSWORD = "123";
    public static final String USER_PASSWORD_NEW = "12345";
    public static final String USER_PASSWORD_ENCODED = "encoded";

    public static final Long ID_INVALID = 99999L;
    public static final Long ID_VALID = 42L;
    public static final Long ID_ADMIN = 777L;
    public static final String ID_STRING = "42";
    public static final Long ID_EXPENSE = 80L;
    public static final Long ID_TRANSACTION = 50L;
    public static final Long ID_CATEGORY = 60L;

    public static final String API_TEST_ENDPOINT = "/api/test";
    public static final String API_AUTH_REGISTER = "/api/auth/register";
    public static final String API_AUTH_LOGIN = "/api/auth/login";
    public static final String API_AUTH_REFRESH = "/api/auth/refresh";
    public static final String API_ADMIN_USERS = "/api/admin/users";
    public static final String API_ADMIN_USERS_CREATE_MODERATOR = "/api/admin/users/create/moderator";
    public static final String API_ADMIN_USERS_CREATE_ADMINISTRATOR = "/api/admin/users/create/administrator";
    public static final String API_USERS_ME = "/api/users/me";
    public static final String API_USERS_CHANGE_PASSWORD = "/api/users/change-password";
    public static final String API_EXPENSES_REPORT = "/api/expenses/report";
    public static final String API_EXPENSES_TOTAL = "/api/expenses/total";
    public static final String API_EXPENSES_STATS_MONTHLY = "/api/expenses/stats/monthly";
    public static final String API_RECURRING_TRANSACTION = "/api/recurring-transaction";
    public static final String API_RECURRING_TRANSACTION_CREATE = "/api/recurring-transaction/create";
    public static final String API_MODERATOR_USERS = "/api/moderator/users";
    
    
    public static final String TYPE_ERROR_WARN = "WARN";
    public static final String TEST_MESSAGE = "Test message";
    public static final String LOGGER_TEST_DATA = "LoggerTestData";
    public static final String CATEGORY_NAME = "food";
    
    public static final String DESCRIPTION = "description";
    public static final int INTERVAL_DAYS = 30;
    public static final int AMOUNT = 500;
    public static final int AMOUNT_NEGATIVE = -500;

    public static final String TOKEN_ACCESS = "access token";
    public static final String TOKEN_REFRESH = "refresh token";

    private Constants() {
    }
}
