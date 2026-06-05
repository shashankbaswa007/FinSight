package com.finsight.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_transactions", indexes = {
    @Index(name = "idx_external_user_date", columnList = "user_id,transaction_date"),
    @Index(name = "idx_external_source", columnList = "source")
})
public class ExternalTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 255)
    private String externalId;
    
    @Column(nullable = false, length = 100)
    private String source;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDate transactionDate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime importedAt;
    
    // Constructors
    public ExternalTransaction() {}
    
    public ExternalTransaction(User user, String externalId, String source, BigDecimal amount, LocalDate transactionDate) {
        this.user = user;
        this.externalId = externalId;
        this.source = source;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    
    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
}
