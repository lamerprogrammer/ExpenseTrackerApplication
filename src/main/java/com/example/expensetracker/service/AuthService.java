package com.example.expensetracker.service;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.User;

public interface AuthService {
    User register(RegisterDto dto);
    TokenResponse login(LoginRequest dto);
    TokenResponse refresh(RefreshRequest request);
    
}
