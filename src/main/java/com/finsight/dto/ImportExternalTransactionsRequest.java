package com.finsight.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ImportExternalTransactionsRequest {
    
    @NotBlank(message = "Source is required (e.g., BANK, CSV_UPLOAD)")
    private String source;
    
    @NotEmpty(message = "Transactions list cannot be empty")
    @Valid
    private List<ExternalTransactionItem> transactions;
    
    public ImportExternalTransactionsRequest() {}
    
    public ImportExternalTransactionsRequest(String source, List<ExternalTransactionItem> transactions) {
        this.source = source;
        this.transactions = transactions;
    }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public List<ExternalTransactionItem> getTransactions() { return transactions; }
    public void setTransactions(List<ExternalTransactionItem> transactions) { this.transactions = transactions; }
    
    public static class ExternalTransactionItem {
        
        @NotBlank(message = "External transaction ID is required")
        private String externalId;
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        private LocalDate transactionDate;
        
        private String description;
        
        public ExternalTransactionItem() {}
        
        public ExternalTransactionItem(String externalId, BigDecimal amount, LocalDate transactionDate, String description) {
            this.externalId = externalId;
            this.amount = amount;
            this.transactionDate = transactionDate;
            this.description = description;
        }
        
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
