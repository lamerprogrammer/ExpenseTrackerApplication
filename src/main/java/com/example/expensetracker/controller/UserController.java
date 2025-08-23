package com.example.expensetracker.controller;


import com.example.expensetracker.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/users/me")
    public User getCurrentUser(@AuthenticationPrincipal User user) {
        return user;
    }
}
