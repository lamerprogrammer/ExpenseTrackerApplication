package com.example.expensetracker.dto;

import com.example.expensetracker.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserResponseDto {


    private final Long id;

    private final String email;

    public UserResponseDto(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }
    
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(user.getId(), user.getEmail());
    }

    public static List<UserResponseDto> fromEntities(List<User> users) {
        return users.stream()
                .map(user -> new UserResponseDto(user.getId(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
