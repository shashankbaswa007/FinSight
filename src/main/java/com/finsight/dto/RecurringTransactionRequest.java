package com.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finsight.model.RecurringFrequency;
import com.finsight.model.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RecurringTransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private TransactionType type;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String description;

    @NotNull(message = "Frequency is required")
    private RecurringFrequency frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RecurringFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurringFrequency frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
