package com.finsight.dto;

import java.math.BigDecimal;

public class TopDescriptionResponse {

    private String description;
    private BigDecimal totalAmount;
    private long count;

    public TopDescriptionResponse() {}

    public TopDescriptionResponse(String description, BigDecimal totalAmount, long count) {
        this.description = description;
        this.totalAmount = totalAmount;
        this.count = count;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public static TopDescriptionResponseBuilder builder() { return new TopDescriptionResponseBuilder(); }

    public static class TopDescriptionResponseBuilder {
        private String description;
        private BigDecimal totalAmount;
        private long count;

        public TopDescriptionResponseBuilder description(String description) { this.description = description; return this; }
        public TopDescriptionResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public TopDescriptionResponseBuilder count(long count) { this.count = count; return this; }

        public TopDescriptionResponse build() { return new TopDescriptionResponse(description, totalAmount, count); }
    }
}
