package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;

public interface UserService {
    User getCurrentUser(UserDetailsImpl user);
    void changePassword(UserDetailsImpl currentUser, ChangePasswordRequest dto);
    BigDecimal getTotalExpenses(Long userId);
    void clearTotalExpensesCache(Long userId);
}

