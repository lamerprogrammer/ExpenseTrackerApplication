package com.example.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public class RegisterDto {

    @NotBlank(message = "{user.name.not-blank}")
    private String name;

    @Email(message = "{user.email.invalid}")
    @NotBlank(message = "{user.email.not-blank}")
    private String email;

    @NotBlank(message = "{user.password.not-blank}")
    private String password;

    public RegisterDto() {}

    public RegisterDto(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterDto that = (RegisterDto) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, password);
    }

    @Override
    public String toString() {
        return "RegisterDto{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password=****}";
    }
}
