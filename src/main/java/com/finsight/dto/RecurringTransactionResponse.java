package com.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finsight.model.RecurringFrequency;
import com.finsight.model.TransactionType;

public class RecurringTransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private Long categoryId;
    private String categoryName;
    private String description;
    private RecurringFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextOccurrence;
    private boolean active;

    public RecurringTransactionResponse() {}

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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RecurringTransactionResponse r = new RecurringTransactionResponse();

        public Builder id(Long id) { r.id = id; return this; }
        public Builder amount(BigDecimal amount) { r.amount = amount; return this; }
        public Builder type(TransactionType type) { r.type = type; return this; }
        public Builder categoryId(Long categoryId) { r.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { r.categoryName = categoryName; return this; }
        public Builder description(String description) { r.description = description; return this; }
        public Builder frequency(RecurringFrequency frequency) { r.frequency = frequency; return this; }
        public Builder startDate(LocalDate startDate) { r.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { r.endDate = endDate; return this; }
        public Builder nextOccurrence(LocalDate nextOccurrence) { r.nextOccurrence = nextOccurrence; return this; }
        public Builder active(boolean active) { r.active = active; return this; }

        public RecurringTransactionResponse build() { return r; }
    }
}
