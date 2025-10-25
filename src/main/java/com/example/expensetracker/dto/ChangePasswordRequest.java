package com.example.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "{change.password.request.old.not-blank}")
        String oldPassword,
        
        @NotBlank(message = "{change.password.request.new.not-blank}")
        String newPassword
) {}
