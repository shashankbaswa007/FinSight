package com.finsight.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a registered user in the system.
 * Each user can have many transactions and budgets.
 */
@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    // ──── GDPR Support (Article 17: Right to Erasure, Article 20: Data Portability) ────
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted = false;

    @Column(name = "deletion_requested_at")
    private LocalDateTime deletionRequestedAt;

    @Column(name = "deletion_reason", length = 255)
    private String deletionReason;

    @Column(name = "hard_delete_scheduled_at")
    private LocalDateTime hardDeleteScheduledAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ──── Telegram Integration ────
    @Column(name = "telegram_chat_id", unique = true)
    private Long telegramChatId;

    @Column(name = "telegram_linking_code", length = 6)
    private String telegramLinkingCode;

    public User() {}

    public User(Long id, String name, String email, String password, Role role,
                LocalDateTime createdAt, List<Transaction> transactions, List<Budget> budgets) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.transactions = transactions;
        this.budgets = budgets;
        this.isDeleted = false;
    }

    // ──── GDPR Anonymization ────

    /**
     * Anonymizes sensitive user fields for GDPR compliance.
     * 
     * IMPORTANT: This must be called EXPLICITLY by GdprService during soft-delete,
     * NOT via @PreUpdate. Using @PreUpdate caused data destruction whenever the
     * user record was saved for any reason (e.g., cancel-deletion would trigger
     * anonymization before the isDeleted flag was cleared).
     * 
     * @param anonymizedEmail A unique anonymized email to replace the real one
     */
    public void anonymize(String anonymizedEmail) {
        this.email = anonymizedEmail;
        this.name = "[DELETED USER]";
        // Password is set to a long random value that can never match any BCrypt hash,
        // rather than a recognizable string that could confuse debugging
        this.password = "ANONYMIZED-" + java.util.UUID.randomUUID();
    }

    // ──── Getters & Setters ────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getDeletionRequestedAt() { return deletionRequestedAt; }
    public void setDeletionRequestedAt(LocalDateTime deletionRequestedAt) { 
        this.deletionRequestedAt = deletionRequestedAt; 
    }

    public String getDeletionReason() { return deletionReason; }
    public void setDeletionReason(String deletionReason) { this.deletionReason = deletionReason; }

    public LocalDateTime getHardDeleteScheduledAt() { return hardDeleteScheduledAt; }
    public void setHardDeleteScheduledAt(LocalDateTime hardDeleteScheduledAt) { 
        this.hardDeleteScheduledAt = hardDeleteScheduledAt; 
    }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Long getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(Long telegramChatId) { this.telegramChatId = telegramChatId; }

    public String getTelegramLinkingCode() { return telegramLinkingCode; }
    public void setTelegramLinkingCode(String telegramLinkingCode) { this.telegramLinkingCode = telegramLinkingCode; }

    // ──── Builder ────

    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Role role = Role.USER;
        private LocalDateTime createdAt;
        private List<Transaction> transactions = new ArrayList<>();
        private List<Budget> budgets = new ArrayList<>();
        private boolean isDeleted = false;
        private LocalDateTime deletionRequestedAt;
        private String deletionReason;
        private LocalDateTime hardDeleteScheduledAt;
        private LocalDateTime lastLogin;
        private Long telegramChatId;
        private String telegramLinkingCode;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserBuilder transactions(List<Transaction> transactions) { this.transactions = transactions; return this; }
        public UserBuilder budgets(List<Budget> budgets) { this.budgets = budgets; return this; }
        public UserBuilder isDeleted(boolean isDeleted) { this.isDeleted = isDeleted; return this; }
        public UserBuilder deletionRequestedAt(LocalDateTime deletionRequestedAt) { 
            this.deletionRequestedAt = deletionRequestedAt; return this; 
        }
        public UserBuilder deletionReason(String deletionReason) { 
            this.deletionReason = deletionReason; return this; 
        }
        public UserBuilder hardDeleteScheduledAt(LocalDateTime hardDeleteScheduledAt) { 
            this.hardDeleteScheduledAt = hardDeleteScheduledAt; return this; 
        }
        public UserBuilder lastLogin(LocalDateTime lastLogin) { 
            this.lastLogin = lastLogin; return this; 
        }
        public UserBuilder telegramChatId(Long telegramChatId) {
            this.telegramChatId = telegramChatId; return this;
        }
        public UserBuilder telegramLinkingCode(String telegramLinkingCode) {
            this.telegramLinkingCode = telegramLinkingCode; return this;
        }

        public User build() {
            User user = new User(id, name, email, password, role, createdAt, transactions, budgets);
            user.isDeleted = this.isDeleted;
            user.deletionRequestedAt = this.deletionRequestedAt;
            user.deletionReason = this.deletionReason;
            user.hardDeleteScheduledAt = this.hardDeleteScheduledAt;
            user.lastLogin = this.lastLogin;
            user.telegramChatId = this.telegramChatId;
            user.telegramLinkingCode = this.telegramLinkingCode;
            return user;
        }
    }
}
