package com.example.expensetracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Where(clause = "deleted = false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "{user.email.invalid}")
    @NotBlank(message = "{user.email.not-blank}")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "{user.password.not-blank}")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "banned", nullable = false)
    private boolean banned = false;
    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "total_expenses", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    public User() {}

    public User(Long id, String email, String password, Set<Role> roles,  boolean banned) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.banned = banned;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public void increaseTotalExpenses(BigDecimal amount) {
        this.totalExpenses = this.totalExpenses.add(amount);
    }

    public void decreaseTotalExpenses(BigDecimal amount) {
        this.totalExpenses = this.totalExpenses.subtract(amount);
    }

    public static class UserBuilder {
        private Long id;
        private String email;
        private String password;
        private Set<Role> roles = new HashSet<>();
        private boolean banned = false;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public UserBuilder banned(boolean banned) {
            this.banned = banned;
            return this;
        }

        public User build() {
            return new User(id, email, password, roles,  banned);
        }
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }
}
