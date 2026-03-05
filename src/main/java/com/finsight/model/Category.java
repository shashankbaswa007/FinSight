package com.finsight.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a transaction category (e.g., Food, Salary).
 * Categories are shared across users and classified by type.
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "type"})
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Budget> budgets = new ArrayList<>();

    public Category() {}

    public Category(Long id, String name, TransactionType type,
                    List<Transaction> transactions, List<Budget> budgets) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.transactions = transactions;
        this.budgets = budgets;
    }

    // ──── Getters & Setters ────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }

    // ──── Builder ────

    public static CategoryBuilder builder() { return new CategoryBuilder(); }

    public static class CategoryBuilder {
        private Long id;
        private String name;
        private TransactionType type;
        private List<Transaction> transactions = new ArrayList<>();
        private List<Budget> budgets = new ArrayList<>();

        public CategoryBuilder id(Long id) { this.id = id; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder type(TransactionType type) { this.type = type; return this; }
        public CategoryBuilder transactions(List<Transaction> transactions) { this.transactions = transactions; return this; }
        public CategoryBuilder budgets(List<Budget> budgets) { this.budgets = budgets; return this; }

        public Category build() {
            return new Category(id, name, type, transactions, budgets);
        }
    }
}
