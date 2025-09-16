package com.example.expensetracker.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RefreshRequest;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.dto.TokenResponse;
import com.example.expensetracker.model.User;

public interface AuthService {
    User register(RegisterDto dto);
    TokenResponse login(LoginDto dto);
    TokenResponse refresh(RefreshRequest request);
    
}
