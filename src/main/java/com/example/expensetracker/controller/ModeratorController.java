package com.example.expensetracker.controller;

import com.example.expensetracker.model.AuditLog;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;

import static com.example.expensetracker.model.AuditAction.BAN;
import static com.example.expensetracker.model.AuditAction.UNBAN;

@RestController
@RequestMapping("/api/mod/users")
public class ModeratorController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ModeratorController(UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}/ban")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> banUser(@PathVariable Long id,
                                     @AuthenticationPrincipal User currentUser) {
        return userRepository.findById(id)
                .map(user -> {
                    if (!user.getRoles().contains(Role.ADMIN) && !user.getRoles().contains(Role.MODERATOR)) {
                        user.setBanned(true);
                        userRepository.save(user);
                        auditLogRepository.save(new AuditLog(BAN, user.getId(), currentUser.getEmail()));
                        return ResponseEntity.ok("Пользователь" + user.getEmail() + " заблокирован");
                    }
                    return ResponseEntity.ok("Пользователь" + user.getEmail() + " не заблокирован, так как он " +
                            user.getRoles().iterator().next());//тут наверно можно лучше сделать
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/unban")
    @PreAuthorize("hasRole('MODERATOR')")
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
}
