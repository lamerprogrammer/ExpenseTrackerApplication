//package test.dto;
//
//import com.example.expensetracker.dto.ApiResponse;
//import com.example.expensetracker.dto.ApiResponseFactory;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//
//import static com.example.expensetracker.dto.ApiResponseFactory.unauthorized;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static test.util.Constants.API_TEST_ENDPOINT;
//
//@ExtendWith(MockitoExtension.class)
//public class ApiResponseFactoryTest {
//    
//    @Mock
//    HttpServletRequest request;
//
//    @InjectMocks
//    ApiResponseFactory apiResponseFactory;
//
//    private final String path = API_TEST_ENDPOINT;
//    
//    @Test
//    void success_shouldReturn200() {
//        
//    }
//
//    @Test
//    void error_shouldReturn200() {
//
//    }
//
//    @Test
//    void validationError_shouldReturn200() {
//
//    }
//
//    @Test
//    void unauthorized_shouldReturnResponse401() {
//        var result = apiResponseFactory.unauthorized(request);
//
//        checkResult(result, HttpStatus.UNAUTHORIZED, "Доступ запрещён: требуется авторизация");
//    }
//
//    @Test
//    void forbidden_shouldReturnResponse403() {
//        var result = apiResponseFactory.forbidden(request);
//
//        checkResult(result, HttpStatus.FORBIDDEN, "У вас недостаточно прав для доступа к этому ресурсу");
//    }
//
//    private void checkResult(ApiResponse<?> result, HttpStatus status, String message) {
//        assertThat(result)
//                .extracting(ApiResponse::getStatus, ApiResponse::getMessage, ApiResponse::getPath)
//                .containsExactly(status.value(), message, path);
//    }
//}
//
