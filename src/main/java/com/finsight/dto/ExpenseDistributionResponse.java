package com.finsight.dto;

import java.math.BigDecimal;

public class ExpenseDistributionResponse {

    private String range;
    private long count;
    private BigDecimal totalAmount;

    public ExpenseDistributionResponse() {}

    public ExpenseDistributionResponse(String range, long count, BigDecimal totalAmount) {
        this.range = range;
        this.count = count;
        this.totalAmount = totalAmount;
    }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public static ExpenseDistributionResponseBuilder builder() { return new ExpenseDistributionResponseBuilder(); }

    public static class ExpenseDistributionResponseBuilder {
        private String range;
        private long count;
        private BigDecimal totalAmount;

        public ExpenseDistributionResponseBuilder range(String range) { this.range = range; return this; }
        public ExpenseDistributionResponseBuilder count(long count) { this.count = count; return this; }
        public ExpenseDistributionResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }

        public ExpenseDistributionResponse build() { return new ExpenseDistributionResponse(range, count, totalAmount); }
    }
}
