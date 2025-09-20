package com.example.expensetracker.controller;

import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.UserResponseDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;


    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getAllUsers() {
        return UserResponseDto.fromEntities(adminService.getAllUsers());
    }

    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> banUser(@PathVariable Long id,
                                          @AuthenticationPrincipal User currentUser) {
        User user = adminService.banUser(id, currentUser);
        return ResponseEntity.ok("Пользователь " + user.getEmail() + " заблокирован");
    }

    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unbanUser(@PathVariable Long id,
                                            @AuthenticationPrincipal User currentUser) {
        User user = adminService.unbanUser(id, currentUser);
        return ResponseEntity.ok("Пользователь " + user.getEmail() + " разблокирован");
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id,
                                             @AuthenticationPrincipal User currentUser) {
        User user = adminService.deleteUser(id, currentUser);
        return ResponseEntity.ok("Пользователь " + user.getEmail() + " удалён");
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createAdmin(@Valid @RequestBody RegisterDto dto) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(adminService.createAdmin(dto)));
    }
}
