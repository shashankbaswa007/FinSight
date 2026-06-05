package com.finsight.dto;

import com.finsight.model.TransactionMatch;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionMatchResponse {
    private Long id;
    private Long internalTransactionId;
    private String externalTransactionId;
    private BigDecimal externalAmount;
    private BigDecimal internalAmount;
    private BigDecimal matchConfidence;
    private String matchStatus;
    private BigDecimal varianceAmount;
    private BigDecimal variancePercentage;
    private String notes;
    private LocalDateTime matchedAt;
    
    public TransactionMatchResponse() {}
    
    public TransactionMatchResponse(Long id, Long internalTransactionId, String externalTransactionId,
                                   BigDecimal externalAmount, BigDecimal internalAmount,
                                   BigDecimal matchConfidence, String matchStatus,
                                   BigDecimal varianceAmount, BigDecimal variancePercentage,
                                   String notes, LocalDateTime matchedAt) {
        this.id = id;
        this.internalTransactionId = internalTransactionId;
        this.externalTransactionId = externalTransactionId;
        this.externalAmount = externalAmount;
        this.internalAmount = internalAmount;
        this.matchConfidence = matchConfidence;
        this.matchStatus = matchStatus;
        this.varianceAmount = varianceAmount;
        this.variancePercentage = variancePercentage;
        this.notes = notes;
        this.matchedAt = matchedAt;
    }
    
    public static TransactionMatchResponse fromEntity(TransactionMatch match) {
        return new TransactionMatchResponse(
            match.getId(),
            match.getInternalTransaction().getId(),
            match.getExternalTransactionId(),
            match.getExternalAmount(),
            match.getInternalAmount(),
            match.getMatchConfidence(),
            match.getMatchStatus().name(),
            match.getVarianceAmount(),
            match.getVariancePercentage(),
            match.getNotes(),
            match.getMatchedAt()
        );
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getInternalTransactionId() { return internalTransactionId; }
    public void setInternalTransactionId(Long internalTransactionId) { this.internalTransactionId = internalTransactionId; }
    
    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
    
    public BigDecimal getExternalAmount() { return externalAmount; }
    public void setExternalAmount(BigDecimal externalAmount) { this.externalAmount = externalAmount; }
    
    public BigDecimal getInternalAmount() { return internalAmount; }
    public void setInternalAmount(BigDecimal internalAmount) { this.internalAmount = internalAmount; }
    
    public BigDecimal getMatchConfidence() { return matchConfidence; }
    public void setMatchConfidence(BigDecimal matchConfidence) { this.matchConfidence = matchConfidence; }
    
    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }
    
    public BigDecimal getVarianceAmount() { return varianceAmount; }
    public void setVarianceAmount(BigDecimal varianceAmount) { this.varianceAmount = varianceAmount; }
    
    public BigDecimal getVariancePercentage() { return variancePercentage; }
    public void setVariancePercentage(BigDecimal variancePercentage) { this.variancePercentage = variancePercentage; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
}
