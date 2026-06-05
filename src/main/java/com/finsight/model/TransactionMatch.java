package com.finsight.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_matches", indexes = {
    @Index(name = "idx_match_batch", columnList = "reconciliation_batch_id"),
    @Index(name = "idx_match_status", columnList = "match_status")
})
public class TransactionMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_batch_id", nullable = false)
    private ReconciliationBatch reconciliationBatch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_transaction_id", nullable = false)
    private Transaction internalTransaction;
    
    @Column
    private String externalTransactionId;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal externalAmount;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal internalAmount;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal matchConfidence = new BigDecimal("100.00");
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus matchStatus;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal varianceAmount = BigDecimal.ZERO;
    
    @Column
    private LocalDateTime matchedAt;
    
    @Column(length = 500)
    private String notes;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum MatchStatus {
        EXACT_MATCH, PARTIAL_MATCH, NO_MATCH
    }
    
    // Constructors
    public TransactionMatch() {}
    
    public TransactionMatch(ReconciliationBatch batch, Transaction transaction) {
        this.reconciliationBatch = batch;
        this.internalTransaction = transaction;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ReconciliationBatch getReconciliationBatch() { return reconciliationBatch; }
    public void setReconciliationBatch(ReconciliationBatch reconciliationBatch) { this.reconciliationBatch = reconciliationBatch; }
    
    public Transaction getInternalTransaction() { return internalTransaction; }
    public void setInternalTransaction(Transaction internalTransaction) { this.internalTransaction = internalTransaction; }
    
    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
    
    public BigDecimal getExternalAmount() { return externalAmount; }
    public void setExternalAmount(BigDecimal externalAmount) { this.externalAmount = externalAmount; }
    
    public BigDecimal getInternalAmount() { return internalAmount; }
    public void setInternalAmount(BigDecimal internalAmount) { this.internalAmount = internalAmount; }
    
    public BigDecimal getMatchConfidence() { return matchConfidence; }
    public void setMatchConfidence(BigDecimal matchConfidence) { this.matchConfidence = matchConfidence; }
    
    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }
    
    public BigDecimal getVarianceAmount() { return varianceAmount; }
    public void setVarianceAmount(BigDecimal varianceAmount) { this.varianceAmount = varianceAmount; }
    
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public BigDecimal getVariancePercentage() {
        if (internalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return varianceAmount.abs()
            .divide(internalAmount, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }
}
