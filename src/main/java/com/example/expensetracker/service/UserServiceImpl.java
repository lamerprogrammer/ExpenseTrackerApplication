package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User getCurrentUser(UserDetailsImpl userDetails) {
        return userDetails.getDomainUser();
    }
}
