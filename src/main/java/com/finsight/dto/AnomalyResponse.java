package com.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a detected spending anomaly using z-score analysis.
 */
public class AnomalyResponse {

    private Long transactionId;
    private BigDecimal amount;
    private String categoryName;
    private String description;
    private LocalDate date;
    private double zScore;
    private String severity; // LOW, MEDIUM, HIGH

    public AnomalyResponse() {}

    public AnomalyResponse(Long transactionId, BigDecimal amount, String categoryName,
                           String description, LocalDate date, double zScore, String severity) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.categoryName = categoryName;
        this.description = description;
        this.date = date;
        this.zScore = zScore;
        this.severity = severity;
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @JsonProperty("zScore")
    public double getZScore() { return zScore; }
    @JsonProperty("zScore")
    public void setZScore(double zScore) { this.zScore = zScore; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public static AnomalyResponseBuilder builder() { return new AnomalyResponseBuilder(); }

    public static class AnomalyResponseBuilder {
        private Long transactionId;
        private BigDecimal amount;
        private String categoryName;
        private String description;
        private LocalDate date;
        private double zScore;
        private String severity;

        public AnomalyResponseBuilder transactionId(Long transactionId) { this.transactionId = transactionId; return this; }
        public AnomalyResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public AnomalyResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public AnomalyResponseBuilder description(String description) { this.description = description; return this; }
        public AnomalyResponseBuilder date(LocalDate date) { this.date = date; return this; }
        public AnomalyResponseBuilder zScore(double zScore) { this.zScore = zScore; return this; }
        public AnomalyResponseBuilder severity(String severity) { this.severity = severity; return this; }

        public AnomalyResponse build() {
            return new AnomalyResponse(transactionId, amount, categoryName, description, date, zScore, severity);
        }
    }
}
