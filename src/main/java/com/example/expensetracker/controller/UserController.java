package com.example.expensetracker.controller;


import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "user.tag.name", description = "user.tag.desc")
@RestController
@RequestMapping("/api/users")
public class UserController implements ControllerSupport {

    private final UserService userService;
    private final MessageSource messageSource;


    public UserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping("/me")
    @Operation(
            summary = "user.get.current.user.sum",
            description = "user.get.current.user.desc")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                               HttpServletRequest request) {
        User user = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("user.controller.get.current.user"), request));
    }

    @PutMapping("/change-password")
    @Operation(
            summary = "user.change.password.sum",
            description = "user.change.password.desc")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest requestDto,
                                                              @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                              HttpServletRequest request) {
        userService.changePassword(currentUser, requestDto);
        return ResponseEntity.ok(ApiResponseFactory.success(null,
                msg("user.controller.password.changed.success"), request));
    }
}

