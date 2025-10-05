package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ModeratorService {

    Page<User> getAllUsers(Pageable pageable);

    User banUser(Long id, UserDetailsImpl currentUser);

    User unbanUser(Long id, UserDetailsImpl currentUser);

    User getUserById(Long id);
}
