package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public final class LoginDto {

    @Email(message = "{user.email.invalid}")
    @NotBlank(message = "{user.email.not-blank}")
    private final String email;

    @NotBlank(message = "{user.password.not-blank}")
    private final String password;

    @JsonCreator
    public LoginDto(@JsonProperty("email") String email,
                    @JsonProperty("password") String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginDto loginDto = (LoginDto) o;
        return Objects.equals(email, loginDto.email) && Objects.equals(password, loginDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "LoginDto{" +
                "email='" + email + '\'' +
                ", password=****}";
    }
}
