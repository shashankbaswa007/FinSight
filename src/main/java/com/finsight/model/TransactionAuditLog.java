package com.finsight.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit trail entity for transaction changes.
 * Records every CREATE, UPDATE, DELETE operation on transactions
 * for regulatory compliance and traceability.
 */
@Entity
@Table(name = "transaction_audit_log", indexes = {
        @Index(name = "idx_audit_transaction", columnList = "transaction_id"),
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_performed_at", columnList = "performed_at")
})
public class TransactionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditAction action;

    @Column(name = "old_amount", precision = 15, scale = 2)
    private BigDecimal oldAmount;

    @Column(name = "new_amount", precision = 15, scale = 2)
    private BigDecimal newAmount;

    @Column(name = "old_type", length = 10)
    private String oldType;

    @Column(name = "new_type", length = 10)
    private String newType;

    @Column(name = "old_category", length = 100)
    private String oldCategory;

    @Column(name = "new_category", length = 100)
    private String newCategory;

    @Column(name = "old_description", length = 500)
    private String oldDescription;

    @Column(name = "new_description", length = 500)
    private String newDescription;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    public TransactionAuditLog() {}

    @PrePersist
    void prePersist() {
        if (performedAt == null) performedAt = LocalDateTime.now();
    }

    // ──── Getters & Setters ────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public BigDecimal getOldAmount() { return oldAmount; }
    public void setOldAmount(BigDecimal oldAmount) { this.oldAmount = oldAmount; }

    public BigDecimal getNewAmount() { return newAmount; }
    public void setNewAmount(BigDecimal newAmount) { this.newAmount = newAmount; }

    public String getOldType() { return oldType; }
    public void setOldType(String oldType) { this.oldType = oldType; }

    public String getNewType() { return newType; }
    public void setNewType(String newType) { this.newType = newType; }

    public String getOldCategory() { return oldCategory; }
    public void setOldCategory(String oldCategory) { this.oldCategory = oldCategory; }

    public String getNewCategory() { return newCategory; }
    public void setNewCategory(String newCategory) { this.newCategory = newCategory; }

    public String getOldDescription() { return oldDescription; }
    public void setOldDescription(String oldDescription) { this.oldDescription = oldDescription; }

    public String getNewDescription() { return newDescription; }
    public void setNewDescription(String newDescription) { this.newDescription = newDescription; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
