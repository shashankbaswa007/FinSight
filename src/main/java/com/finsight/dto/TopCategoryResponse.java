package com.finsight.dto;

import java.math.BigDecimal;

/**
 * DTO representing a top-spending category with its total amount.
 */
public class TopCategoryResponse {

    private String categoryName;
    private BigDecimal totalAmount;
    private long transactionCount;

    public TopCategoryResponse() {}

    public TopCategoryResponse(String categoryName, BigDecimal totalAmount, long transactionCount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }

    public static TopCategoryResponseBuilder builder() { return new TopCategoryResponseBuilder(); }

    public static class TopCategoryResponseBuilder {
        private String categoryName;
        private BigDecimal totalAmount;
        private long transactionCount;

        public TopCategoryResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public TopCategoryResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public TopCategoryResponseBuilder transactionCount(long transactionCount) { this.transactionCount = transactionCount; return this; }

        public TopCategoryResponse build() {
            return new TopCategoryResponse(categoryName, totalAmount, transactionCount);
        }
    }
}
