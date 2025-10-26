package com.example.expensetracker.controller;


import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;


    public UserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                               HttpServletRequest request) {
        User user = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("user.controller.get.current.user"), request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest requestDto,
                                                               @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                               HttpServletRequest request) {
        userService.changePassword(currentUser, requestDto);
        return ResponseEntity.ok(ApiResponseFactory.success(null,
                msg("user.controller.password.changed.success"), request));
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
