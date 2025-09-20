package com.example.expensetracker.controller;


import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getCurrentUser(userDetails);
    }
}
