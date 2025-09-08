package com.example.expensetracker.controller;

import com.example.expensetracker.model.AuditLog;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.expensetracker.model.AuditAction.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> banUser(@PathVariable Long id,
                                     @AuthenticationPrincipal User currentUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBanned(true);
                    userRepository.save(user);

                    auditLogRepository.save(new AuditLog(BAN, user.getId(), currentUser.getEmail()));

                    return ResponseEntity.ok("Пользователь" + user.getEmail() + " заблокирован");
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unbanUser(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBanned(false);
                    userRepository.save(user);

                    auditLogRepository.save(new AuditLog(UNBAN, user.getId(), currentUser.getEmail()));

                    return ResponseEntity.ok("Пользователь" + user.getEmail() + " разблокирован");
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal User currentUser) {
        if (!userRepository.existsById(id)) {
            return  ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);

        auditLogRepository.save(new AuditLog(DELETE, id, currentUser.getEmail()));

        return ResponseEntity.noContent().build();
    }
}
