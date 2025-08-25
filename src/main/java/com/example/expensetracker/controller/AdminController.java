package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBanned(true);
                    userRepository.save(user);
                    return ResponseEntity.ok("Пользователь" + user.getEmail() + " заблокирован");
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unbanUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBanned(false);
                    userRepository.save(user);
                    return ResponseEntity.ok("Пользователь" + user.getEmail() + " разблокирован");
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        if (!userRepository.existsById(id)) {
            return  ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
