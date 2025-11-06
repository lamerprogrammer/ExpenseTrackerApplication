package com.example.expensetracker.controller;

import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.*;
import com.example.expensetracker.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "admin.tag.name", description = "admin.tag.desc")
@RestController
@RequestMapping("/api/admin/users")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController implements ControllerSupport {

    private final AdminService adminService;
    private final MessageSource messageSource;


    public AdminController(AdminService adminService, MessageSource messageSource) {
        this.adminService = adminService;
        this.messageSource = messageSource;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping
    @Operation(
            summary = "admin.get.all.users.sum",
            description = "admin.get.all.users.desc")
    public ResponseEntity<ApiResponse<Page<AdminUserDto>>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") @Parameter(hidden = true) Pageable pageable,
            HttpServletRequest request) {
        Page<AdminUserDto> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(users, msg("get.all.users"), request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "admin.get.by.id.sum",
            description = "admin.get.by.id.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUserById(@PathVariable @Positive long id,
                                                                 HttpServletRequest request) {
        AdminUserDto response = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("get.user.by.id"), request));
    }

    @PutMapping("/{id}/ban")
    @Operation(
            summary = "admin.ban.user.sum",
            description = "admin.ban.user.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> banUser(@PathVariable long id,
                                                             @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                             HttpServletRequest request) {
        AdminUserDto response = adminService.banUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("ban.user"), request));
    }

    @PutMapping("/{id}/unban")
    @Operation(
            summary = "admin.unban.user.sum",
            description = "admin.unban.user.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> unbanUser(@PathVariable long id,
                                                               @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                               HttpServletRequest request) {
        AdminUserDto response = adminService.unbanUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("unban.user"), request));
    }

    @PutMapping("/{id}/promote")
    @Operation(
            summary = "admin.promote.user.sum",
            description = "admin.promote.user.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> promoteUser(@PathVariable long id,
                                                                 @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                 HttpServletRequest request) {
        AdminUserDto response = adminService.promoteUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("promote.user"), request));
    }

    @PutMapping("/{id}/demote")
    @Operation(
            summary = "admin.demote.user.sum",
            description = "admin.demote.user.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> demoteUser(@PathVariable long id,
                                                                @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                HttpServletRequest request) {
        AdminUserDto response = adminService.demoteUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("demote.user"), request));
    }

    @DeleteMapping("/{id}/delete")
    @Operation(
            summary = "admin.delete.user.sum",
            description = "admin.delete.user.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> deleteUser(@PathVariable long id,
                                                                @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                HttpServletRequest request) {
        AdminUserDto response = adminService.deleteUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("delete.user"), request));
    }

    @PostMapping("/create/administrator")
    @Operation(
            summary = "admin.create.admin.sum",
            description = "admin.create.admin.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> createAdmin(@Valid @RequestBody RegisterDto dto,
                                                                 @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                 HttpServletRequest request) {
        AdminUserDto response = adminService.createAdmin(dto, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("create.admin"), request));
    }

    @PostMapping("/create/moderator")
    @Operation(
            summary = "admin.create.moder.sum",
            description = "admin.create.moder.desc")
    public ResponseEntity<ApiResponse<AdminUserDto>> createModer(@Valid @RequestBody RegisterDto dto,
                                                                 @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                 HttpServletRequest request) {
        AdminUserDto response = adminService.createModerator(dto, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(response, msg("create.moder"), request));
    }
}
