package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.AdminUserDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    Page<AdminUserDto> getAllUsers(Pageable pageable);

    AdminUserDto banUser(Long id, UserDetailsImpl currentUser);

    AdminUserDto unbanUser(Long id, UserDetailsImpl currentUser);

    AdminUserDto deleteUser(Long id, UserDetailsImpl currentUser);

    AdminUserDto createAdmin(RegisterDto dto, UserDetailsImpl currentUser);

    AdminUserDto createModerator(RegisterDto dto, UserDetailsImpl currentUser);

    AdminUserDto getUserById(Long id);

    AdminUserDto promoteUser(Long id, UserDetailsImpl currentUser);

    AdminUserDto demoteUser(Long id, UserDetailsImpl currentUser);
}

