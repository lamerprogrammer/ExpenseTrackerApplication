package com.example.expensetracker.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;//'Many To One' attribute type should not be 'Category' а если убрать собачку мэни ту ван, то 'Basic' attribute type should not be 'Category' 
    
    private String description;

    public Expense() {}

    public Expense(Long id, User user, BigDecimal amount, Instant occurredAt, Category category, String description) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.category = category;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
