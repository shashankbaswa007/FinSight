package com.finsight.dto;

import com.finsight.model.ReconciliationBatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReconciliationBatchResponse {
    private Long id;
    private LocalDate batchDate;
    private String status;
    private Integer totalTransactions;
    private Integer matchedTransactions;
    private Integer unmatchedTransactions;
    private BigDecimal discrepancyAmount;
    private Double matchPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ReconciliationBatchResponse() {}
    
    public ReconciliationBatchResponse(Long id, LocalDate batchDate, String status, Integer totalTransactions,
                                      Integer matchedTransactions, Integer unmatchedTransactions,
                                      BigDecimal discrepancyAmount, Double matchPercentage,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.batchDate = batchDate;
        this.status = status;
        this.totalTransactions = totalTransactions;
        this.matchedTransactions = matchedTransactions;
        this.unmatchedTransactions = unmatchedTransactions;
        this.discrepancyAmount = discrepancyAmount;
        this.matchPercentage = matchPercentage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static ReconciliationBatchResponse fromEntity(ReconciliationBatch batch) {
        double matchPercentage = batch.getTotalTransactions() > 0 
            ? (double) batch.getMatchedTransactions() / batch.getTotalTransactions() * 100 
            : 0;
        
        return new ReconciliationBatchResponse(
            batch.getId(),
            batch.getBatchDate(),
            batch.getStatus().name(),
            batch.getTotalTransactions(),
            batch.getMatchedTransactions(),
            batch.getUnmatchedTransactions(),
            batch.getDiscrepancyAmount(),
            matchPercentage,
            batch.getCreatedAt(),
            batch.getUpdatedAt()
        );
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getBatchDate() { return batchDate; }
    public void setBatchDate(LocalDate batchDate) { this.batchDate = batchDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
    
    public Integer getMatchedTransactions() { return matchedTransactions; }
    public void setMatchedTransactions(Integer matchedTransactions) { this.matchedTransactions = matchedTransactions; }
    
    public Integer getUnmatchedTransactions() { return unmatchedTransactions; }
    public void setUnmatchedTransactions(Integer unmatchedTransactions) { this.unmatchedTransactions = unmatchedTransactions; }
    
    public BigDecimal getDiscrepancyAmount() { return discrepancyAmount; }
    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) { this.discrepancyAmount = discrepancyAmount; }
    
    public Double getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(Double matchPercentage) { this.matchPercentage = matchPercentage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
