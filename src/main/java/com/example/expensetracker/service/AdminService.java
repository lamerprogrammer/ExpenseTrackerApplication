package com.example.expensetracker.service;

import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;

import java.util.List;

public interface AdminService {
    List<User> getAllUsers();

    User banUser(Long id, User currentUser);

    User unbanUser(Long id, User currentUser);

    User deleteUser(Long id, User currentUser);

    User createAdmin(RegisterDto dto, User currentUser);
}
