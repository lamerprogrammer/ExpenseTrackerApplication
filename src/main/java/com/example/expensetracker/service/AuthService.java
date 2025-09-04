package com.example.expensetracker.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;

import java.util.Map;

public interface AuthService {
    Map<String, String> register(RegisterDto dto);
    Map<String, String> login(LoginDto dto);
}
