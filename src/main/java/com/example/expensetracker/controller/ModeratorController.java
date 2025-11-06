package com.example.expensetracker.controller;

import com.example.expensetracker.controller.base.ControllerSupport;
import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ApiResponse;
import com.example.expensetracker.dto.ApiResponseFactory;
import com.example.expensetracker.dto.ModeratorUserDto;
import com.example.expensetracker.dto.UserDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.ModeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

@Tag(name = "moder.tag.name", description = "moder.tag.desc")
@RestController
@RequestMapping("/api/moderator/users")
@Validated
@PreAuthorize("hasRole('MODERATOR')")
public class ModeratorController implements ControllerSupport {

    private final ModeratorService moderatorService;
    private final MessageSource messageSource;

    public ModeratorController(ModeratorService moderatorService, MessageSource messageSource) {
        this.moderatorService = moderatorService;
        this.messageSource = messageSource;
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    @GetMapping
    @Operation(
            summary = "moder.get.all.users.sum",
            description = "moder.get.all.users.desc")
    public ResponseEntity<ApiResponse<Page<ModeratorUserDto>>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") @Parameter(hidden = true) Pageable pageable, HttpServletRequest request) {
        Page<ModeratorUserDto> users = moderatorService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponseFactory.success(users, msg("get.all.users"), request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "moder.get.by.id.sum",
            description = "moder.get.by.id.desc")
    public ResponseEntity<ApiResponse<ModeratorUserDto>> getUserById(@PathVariable @Positive long id,
                                                                     HttpServletRequest request) {
        ModeratorUserDto result = moderatorService.getUserById(id);
        return ResponseEntity.ok(ApiResponseFactory.success(result, msg("get.user.by.id"), request));
    }

    @PutMapping("/{id}/ban")
    @Operation(
            summary = "moder.get.ban.user.sum",
            description = "moder.get.ban.user.desc")
    public ResponseEntity<ApiResponse<ModeratorUserDto>> banUser(@PathVariable long id,
                                                                 @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                 HttpServletRequest request) {
        ModeratorUserDto result = moderatorService.banUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(result, msg("ban.user"), request));
    }

    @PutMapping("/{id}/unban")
    @Operation(
            summary = "moder.get.unban.user.sum",
            description = "moder.get.unban.user.desc")
    public ResponseEntity<ApiResponse<ModeratorUserDto>> unbanUser(@PathVariable long id,
                                                                   @AuthenticationPrincipal UserDetailsImpl currentUser,
                                                                   HttpServletRequest request) {
        ModeratorUserDto result = moderatorService.unbanUser(id, currentUser);
        return ResponseEntity.ok(ApiResponseFactory.success(result, msg("unban.user"), request));
    }
}

