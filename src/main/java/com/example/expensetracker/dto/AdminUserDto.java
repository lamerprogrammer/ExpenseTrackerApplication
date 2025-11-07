package com.example.expensetracker.dto;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminUserDto {

    private final Long id;

    private final String email;

    private Set<Role> roles = new HashSet<>();

    private boolean banned = false;

    private boolean deleted = false;

    private BigDecimal totalExpenses = BigDecimal.ZERO;

    public AdminUserDto(@JsonProperty("id") Long id,
                        @JsonProperty("email") String email,
                        @JsonProperty("roles") Set<Role> roles,
                        @JsonProperty("banned") boolean banned,
                        @JsonProperty("deleted") boolean deleted,
                        @JsonProperty("totalExpenses") BigDecimal totalExpenses) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.banned = banned;
        this.deleted = deleted;
        this.totalExpenses = totalExpenses;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public static AdminUserDto fromEntity(User user) {
        return new AdminUserDto(user.getId(), user.getEmail(), user.getRoles(), user.isBanned(),
                user.isDeleted(), user.getTotalExpenses());
    }

    public static List<AdminUserDto> fromEntities(List<User> users) {
        return users.stream()
                .map(user -> new AdminUserDto(user.getId(), user.getEmail(), user.getRoles(), user.isBanned(),
                        user.isDeleted(), user.getTotalExpenses()))
                .collect(Collectors.toList());
    }
}
