package com.example.expensetracker.dto;

import com.example.expensetracker.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.stream.Collectors;

public class UserDto {
    
    private Long id;

    private String email;

    public UserDto(@JsonProperty("id") Long id,
                   @JsonProperty("email") String email) {
        this.id = id;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }
    
    public static UserDto fromEntity(User user) {
        return new UserDto(user.getId(), user.getEmail());
    }

    public static List<UserDto> fromEntities(List<User> users) {
        return users.stream()
                .map(user -> new UserDto(user.getId(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
