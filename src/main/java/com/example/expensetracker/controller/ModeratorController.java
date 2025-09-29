package com.example.expensetracker.controller;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.ModeratorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderator/users")
@Validated
public class ModeratorController {

    private final ModeratorService moderatorService;
    private final MessageSource messageSource;

    public ModeratorController(ModeratorService moderatorService, MessageSource messageSource) {
        this.moderatorService = moderatorService;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(HttpServletRequest request) {
        List<UserDto> users = UserDto.fromEntities(moderatorService.getAllUsers());
        return ResponseEntity.ok(ApiResponseFactory.success(users, msg("get.all.users"), request));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsersInPage(Pageable pageable,
                                                                        HttpServletRequest request) {
        Page<UserDto> users = moderatorService.getAllUsersPaged(pageable).map(UserDto::fromEntity);
        return ResponseEntity.ok(ApiResponseFactory.success(users, msg("get.all.users"), request));
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable @Positive long id,
                                                            HttpServletRequest request) {
        User user = moderatorService.getUserById(id);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("get.user.by.id"), request));
    }

    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserDto>> banUser(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                        HttpServletRequest request) {
        User user = moderatorService.banUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("ban.user"), request));
    }

    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserDto>> unbanUser(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                          HttpServletRequest request) {
        User user = moderatorService.unbanUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("unban.user"), request));
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
