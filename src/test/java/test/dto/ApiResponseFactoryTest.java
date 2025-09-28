//package test.dto;
//
//import com.example.expensetracker.dto.ApiResponseFactory;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//
//import static com.example.expensetracker.dto.ApiResponseFactory.unauthorized;
//import static test.util.Constants.API_TEST_ENDPOINT;
//
//@ExtendWith(MockitoExtension.class)
//public class ApiResponseFactoryTest {
//
//    @InjectMocks
//    ApiResponseFactory apiResponseFactory;
//
//    private final String path = API_TEST_ENDPOINT;
//
//    @Test
//    void unauthorized_shouldReturnResponse401() {
//        var result = unauthorized(path);
//
//        checkResult(result, HttpStatus.UNAUTHORIZED, "Доступ запрещён: требуется авторизация.");
//    }
//
//    @Test
//    void unauthorized_shouldReturnResponse403() {
//        ApiResponse result = apiResponseFactory.forbidden(path);
//
//        checkResult(result, HttpStatus.FORBIDDEN, "У вас недостаточно прав для доступа к этому ресурсу.");
//    }
//
//    private void checkResult(ApiResponse result, HttpStatus status, String message) {
//        assertThat(result)
//                .extracting(ApiResponse::getStatus, ApiResponse::getMessage, ApiResponse::getPath)
//                .containsExactly(status.value(), message, path);
//    }
//}
//
