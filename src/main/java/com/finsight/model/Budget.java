package com.finsight.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * JPA entity representing a monthly budget limit set by a user for a specific category.
 * Used to track overspending against defined limits.
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category_id", "budget_month", "budget_year"})
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    public Budget() {}

    public Budget(Long id, User user, Category category, BigDecimal monthlyLimit, Integer month, Integer year) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.month = month;
        this.year = year;
    }

    // ──── Getters & Setters ────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    // ──── Builder ────

    public static BudgetBuilder builder() { return new BudgetBuilder(); }

    public static class BudgetBuilder {
        private Long id;
        private User user;
        private Category category;
        private BigDecimal monthlyLimit;
        private Integer month;
        private Integer year;

        public BudgetBuilder id(Long id) { this.id = id; return this; }
        public BudgetBuilder user(User user) { this.user = user; return this; }
        public BudgetBuilder category(Category category) { this.category = category; return this; }
        public BudgetBuilder monthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; return this; }
        public BudgetBuilder month(Integer month) { this.month = month; return this; }
        public BudgetBuilder year(Integer year) { this.year = year; return this; }

        public Budget build() {
            return new Budget(id, user, category, monthlyLimit, month, year);
        }
    }
}
