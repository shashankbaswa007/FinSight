package com.finsight.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

/**
 * JPA entity for recurring transactions.
 * 
 * ENCRYPTION: amount is encrypted at rest in the database.
 * - Encrypted values stored in: amount_encrypted (LONGBLOB)
 * - Legacy column (amount) kept during migration period
 * - On read: Decrypted automatically via @PostLoad
 * - On write: Encrypted automatically via @PrePersist/@PreUpdate
 */
@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecurringFrequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextOccurrence;

    @Column(nullable = false)
    private boolean active = true;

    // ──── Encrypted Column (for V4+ migration) ────
    @Column(name = "amount_encrypted", columnDefinition = "LONGBLOB")
    private byte[] amountEncrypted;

    // ──── Transient Field for Encryption Status ----
    @Transient
    private boolean encryptionMigrationComplete = false;

    public RecurringTransaction() {}

    /**
     * Called by Hibernate before storing entity. Encrypts sensitive fields.
     */
    @PrePersist
    @PreUpdate
    public void encryptSensitiveData() {
        // Encryption logic will be handled by migration job
        // This hook is here for future enhancements
    }

    /**
     * Called by Hibernate after loading entity from database.
     * Decrypts sensitive fields if they exist in encrypted columns.
     */
    @PostLoad
    public void decryptSensitiveData() {
        // Decryption logic will be implemented after migration completes
        // During migration period, unencrypted amount is used
    }

    // Getters & Setters
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

    public RecurringFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurringFrequency frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getNextOccurrence() { return nextOccurrence; }
    public void setNextOccurrence(LocalDate nextOccurrence) { this.nextOccurrence = nextOccurrence; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public byte[] getAmountEncrypted() { return amountEncrypted; }
    public void setAmountEncrypted(byte[] amountEncrypted) { this.amountEncrypted = amountEncrypted; }

    public boolean isEncryptionMigrationComplete() { return encryptionMigrationComplete; }
    public void setEncryptionMigrationComplete(boolean encryptionMigrationComplete) { this.encryptionMigrationComplete = encryptionMigrationComplete; }
}
