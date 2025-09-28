package com.example.expensetracker.controller;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Validated
public class AdminController {

    private final AdminService adminService;
    private final MessageSource messageSource;


    public AdminController(AdminService adminService, MessageSource messageSource) {
        this.adminService = adminService;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(HttpServletRequest request) {
        List<UserDto> users = UserDto.fromEntities(adminService.getAllUsers());
        return ResponseEntity.ok(ApiResponseFactory.success(users, msg("get.all.users"), request));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsersInPage(Pageable pageable,
                                                                        HttpServletRequest request) {
        Page<UserDto> users = adminService.getAllUsersPaged(pageable).map(UserDto::fromEntity);
        return ResponseEntity.ok(ApiResponseFactory.success(users, msg("get.all.users"), request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable @Positive long id,
                                                            HttpServletRequest request) {
        User user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("get.user.by.id"), request));
    }

    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> banUser(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                        HttpServletRequest request) {
        User user = adminService.banUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("ban.user"), request));
    }

    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> unbanUser(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                          HttpServletRequest request) {
        User user = adminService.unbanUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("unban.user"), request));
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> deleteUser(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                           HttpServletRequest request) {
        User user = adminService.deleteUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("delete.user"), request));
    }

    @PostMapping("/create/administrator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createAdmin(@Valid @RequestBody RegisterDto dto,
                                                            @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                            HttpServletRequest request) {
        User user = adminService.createAdmin(dto, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("create.admin"), request));
    }

    @PostMapping("/create/moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createModer(@Valid @RequestBody RegisterDto dto,
                                                            @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                            HttpServletRequest request) {
        User user = adminService.createModerator(dto, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(UserDto.fromEntity(user),
                msg("create.moder"), request));
    }
    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
