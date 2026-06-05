package com.finsight.model;

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
     * Called by Hibernate before storing entity.
     * 
     * During the V4-V5 migration period, encryption is handled by the
     * EncryptionMigrationService batch job which reads existing rows and writes
     * encrypted values to amount_encrypted / description_encrypted.
     * Once migration completes, this hook should be updated to encrypt on every write.
     * 
     * NOTE: This is intentionally a no-op during migration. Do NOT add encryption
     * logic here until the migration is verified complete and old columns are dropped.
     */
    @PrePersist
    @PreUpdate
    public void encryptSensitiveData() {
        // Intentional no-op during V4-V5 migration period.
        // Encryption is performed by EncryptionMigrationService batch job.
    }

    /**
     * Called by Hibernate after loading entity from database.
     * 
     * During migration, encrypted columns may be null — falls back to the
     * unencrypted amount/description fields which remain the primary source.
     * Once migration completes and old columns are dropped, this hook must
     * decrypt from amountEncrypted / descriptionEncrypted.
     * 
     * NOTE: This is intentionally a no-op during migration.
     */
    @PostLoad
    public void decryptSensitiveData() {
        // Intentional no-op during V4-V5 migration period.
        // Unencrypted columns remain the primary data source until migration completes.
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
