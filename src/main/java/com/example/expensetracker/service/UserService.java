package com.example.expensetracker.service;

import com.example.expensetracker.dto.LoginDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;

public interface UserService {
    User register(RegisterDto dto);
    User validateUser(LoginDto dto);
    User createAdmin(RegisterDto dto);
}
