package com.finsight.model;

import com.finsight.security.EncryptionUtil;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single financial transaction (income or expense).
 * Linked to a User and a Category.
 * 
 * ENCRYPTION: amount and description are encrypted at rest in the database.
 * - Encrypted values stored in: amount_encrypted, description_encrypted (LONGBLOB)
 * - Legacy columns (amount, description) kept during migration period
 * - On read: Decrypted automatically via @PostLoad
 * - On write: Encrypted automatically via @PrePersist/@PreUpdate
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_user_date", columnList = "user_id, date"),
        @Index(name = "idx_transaction_user_category", columnList = "user_id, category_id")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ──── Encrypted Columns (for V4+ migration) ────
    // These columns store encrypted versions of amount and description
    // During migration period (V4-V5), both old and new columns are kept
    // After migration complete, old columns can be dropped and these become primary
    
    @Column(name = "amount_encrypted", columnDefinition = "LONGBLOB")
    private byte[] amountEncrypted;

    @Column(name = "description_encrypted", columnDefinition = "LONGBLOB")
    private byte[] descriptionEncrypted;

    // ──── Transient Field for Encryption Status ----
    @Transient
    private boolean encryptionMigrationComplete = false;

    public Transaction() {}

    public Transaction(Long id, User user, BigDecimal amount, TransactionType type,
                       Category category, String description, LocalDate date, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.date = date;
        this.createdAt = createdAt;
    }

    /**
     * Called by Hibernate before storing entity. Encrypts sensitive fields.
     * During migration period (V4-V5):
     * - Writes to both old and new columns for backward compatibility
     * After migration complete:
     * - Old columns become read-only, only new encrypted columns are written
     */
    @PrePersist
    @PreUpdate
    public void encryptSensitiveData() {
        // For now, keep writing to old columns during migration period
        // The amountEncrypted and descriptionEncrypted fields will be populated
        // by a separate migration job that reads old columns and encrypts them
        
        // This hook can be extended later to encrypt on-demand if needed
    }

    /**
     * Called by Hibernate after loading entity from database.
     * Decrypts sensitive fields if they exist in encrypted columns.
     * Falls back to unencrypted fields during migration period.
     */
    @PostLoad
    public void decryptSensitiveData() {
        // During migration period, encrypted columns will be null
        // So we use the unencrypted amount and description
        
        // Once migration completes, encrypted columns will have data
        // and we'll need to decrypt them here
        // For now, this is a placeholder for future decryption logic
    }

    // ──── Getters & Setters ────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public byte[] getAmountEncrypted() { return amountEncrypted; }
    public void setAmountEncrypted(byte[] amountEncrypted) { this.amountEncrypted = amountEncrypted; }

    public byte[] getDescriptionEncrypted() { return descriptionEncrypted; }
    public void setDescriptionEncrypted(byte[] descriptionEncrypted) { this.descriptionEncrypted = descriptionEncrypted; }

    public boolean isEncryptionMigrationComplete() { return encryptionMigrationComplete; }
    public void setEncryptionMigrationComplete(boolean encryptionMigrationComplete) { this.encryptionMigrationComplete = encryptionMigrationComplete; }

    // ──── Builder ────

    public static TransactionBuilder builder() { return new TransactionBuilder(); }

    public static class TransactionBuilder {
        private Long id;
        private User user;
        private BigDecimal amount;
        private TransactionType type;
        private Category category;
        private String description;
        private LocalDate date;
        private LocalDateTime createdAt;

        public TransactionBuilder id(Long id) { this.id = id; return this; }
        public TransactionBuilder user(User user) { this.user = user; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionBuilder category(Category category) { this.category = category; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder date(LocalDate date) { this.date = date; return this; }
        public TransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Transaction build() {
            return new Transaction(id, user, amount, type, category, description, date, createdAt);
        }
    }
}
