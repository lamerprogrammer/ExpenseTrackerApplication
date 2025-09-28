package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    List<User> getAllUsers();

    Page<User> getAllUsersPaged(Pageable pageable);
    
    User banUser(Long id, UserDetailsImpl currentUser);

    User unbanUser(Long id, UserDetailsImpl currentUser);

    User deleteUser(Long id, UserDetailsImpl currentUser);

    User createAdmin(RegisterDto dto, UserDetailsImpl currentUser);

    User createModerator(RegisterDto dto, UserDetailsImpl currentUser);

    User getUserById(Long id);
}
