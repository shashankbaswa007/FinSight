package com.finsight.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_batches", indexes = {
    @Index(name = "idx_reconciliation_user_date", columnList = "user_id,batch_date"),
    @Index(name = "idx_reconciliation_status", columnList = "status")
})
public class ReconciliationBatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate batchDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status = ReconciliationStatus.PENDING;
    
    @Column(nullable = false)
    private Integer totalTransactions = 0;
    
    @Column(nullable = false)
    private Integer matchedTransactions = 0;
    
    @Column(nullable = false)
    private Integer unmatchedTransactions = 0;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal discrepancyAmount = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ReconciliationStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
    
    // Constructors
    public ReconciliationBatch() {}
    
    public ReconciliationBatch(User user, LocalDate batchDate) {
        this.user = user;
        this.batchDate = batchDate;
        this.status = ReconciliationStatus.PENDING;
        this.totalTransactions = 0;
        this.matchedTransactions = 0;
        this.unmatchedTransactions = 0;
        this.discrepancyAmount = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDate getBatchDate() { return batchDate; }
    public void setBatchDate(LocalDate batchDate) { this.batchDate = batchDate; }
    
    public ReconciliationStatus getStatus() { return status; }
    public void setStatus(ReconciliationStatus status) { this.status = status; }
    
    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
    
    public Integer getMatchedTransactions() { return matchedTransactions; }
    public void setMatchedTransactions(Integer matchedTransactions) { this.matchedTransactions = matchedTransactions; }
    
    public Integer getUnmatchedTransactions() { return unmatchedTransactions; }
    public void setUnmatchedTransactions(Integer unmatchedTransactions) { this.unmatchedTransactions = unmatchedTransactions; }
    
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
