package com.example.expensetracker.service.util;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {
    
    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User validateAndGetActor(Long id, UserDetailsImpl currentUser) {
        User userEntity = getActor(currentUser);
        if (id.equals(userEntity.getId())) {
            throw new IllegalArgumentException("You cannot perform an action on yourself");
        }
        return userEntity;
    }

    public User getActor(UserDetailsImpl currentUser) {
        return userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void existingActor(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityExistsException("This email is already in use");
        }
    }
}
