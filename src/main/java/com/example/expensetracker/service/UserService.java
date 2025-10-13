package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public interface UserService {
    User getCurrentUser(UserDetailsImpl user);
    void changePassword(UserDetailsImpl currentUser, ChangePasswordRequest dto);
}
