package com.finsight.dto;

import com.finsight.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for transaction response data (never exposes the entity directly).
 */
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private Long categoryId;
    private String categoryName;
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;

    public TransactionResponse() {}

    public TransactionResponse(Long id, BigDecimal amount, TransactionType type, Long categoryId,
                               String categoryName, String description, LocalDate date, LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.date = date;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TransactionResponseBuilder builder() { return new TransactionResponseBuilder(); }

    public static class TransactionResponseBuilder {
        private Long id;
        private BigDecimal amount;
        private TransactionType type;
        private Long categoryId;
        private String categoryName;
        private String description;
        private LocalDate date;
        private LocalDateTime createdAt;

        public TransactionResponseBuilder id(Long id) { this.id = id; return this; }
        public TransactionResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionResponseBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionResponseBuilder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public TransactionResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public TransactionResponseBuilder description(String description) { this.description = description; return this; }
        public TransactionResponseBuilder date(LocalDate date) { this.date = date; return this; }
        public TransactionResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TransactionResponse build() {
            return new TransactionResponse(id, amount, type, categoryId, categoryName, description, date, createdAt);
        }
    }
}
