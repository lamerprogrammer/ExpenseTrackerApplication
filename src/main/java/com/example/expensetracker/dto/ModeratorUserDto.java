package com.example.expensetracker.dto;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModeratorUserDto {

    private final Long id;

    private final String email;

    private Set<Role> roles = new HashSet<>();

    private boolean banned = false;

    private BigDecimal totalExpenses = BigDecimal.ZERO;

    public ModeratorUserDto(@JsonProperty("id") Long id,
                            @JsonProperty("email") String email,
                            @JsonProperty("roles") Set<Role> roles,
                            @JsonProperty("banned") boolean banned,
                            @JsonProperty("totalExpenses") BigDecimal totalExpenses) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.banned = banned;
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

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public static ModeratorUserDto fromEntity(User user) {
        return new ModeratorUserDto(user.getId(), user.getEmail(), user.getRoles(), user.isBanned(),
                user.getTotalExpenses());
    }

    public static List<ModeratorUserDto> fromEntities(List<User> users) {
        return users.stream()
                .map(user -> new ModeratorUserDto(user.getId(), user.getEmail(), user.getRoles(), user.isBanned(),
                        user.getTotalExpenses()))
                .collect(Collectors.toList());
    }
}
