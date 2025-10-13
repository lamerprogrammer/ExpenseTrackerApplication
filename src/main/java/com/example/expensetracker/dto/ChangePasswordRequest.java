package com.example.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "{user.password.old.not-blank}")
        String oldPassword,
        
        @NotBlank(message = "{user.password.new.not-blank}")
        String newPassword
) {}
