package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public final class RegisterDto {

    @NotBlank(message = "{user.name.not-blank}")
    private final String name;

    @Email(message = "{user.email.invalid}")
    @NotBlank(message = "{user.email.not-blank}")
    private final String email;

    @NotBlank(message = "{user.password.not-blank}")
    private final String password;

    @JsonCreator
    public RegisterDto(@JsonProperty("name") String name,
                       @JsonProperty("email") String email,
                       @JsonProperty("password") String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
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
