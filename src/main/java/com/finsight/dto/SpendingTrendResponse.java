package com.finsight.dto;

import java.math.BigDecimal;

/**
 * DTO for monthly spending trend data used in analytics.
 */
public class SpendingTrendResponse {

    private Integer month;
    private Integer year;
    private BigDecimal totalSpending;
    private BigDecimal totalIncome;

    public SpendingTrendResponse() {}

    public SpendingTrendResponse(Integer month, Integer year, BigDecimal totalSpending, BigDecimal totalIncome) {
        this.month = month;
        this.year = year;
        this.totalSpending = totalSpending;
        this.totalIncome = totalIncome;
    }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getTotalSpending() { return totalSpending; }
    public void setTotalSpending(BigDecimal totalSpending) { this.totalSpending = totalSpending; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public static SpendingTrendResponseBuilder builder() { return new SpendingTrendResponseBuilder(); }

    public static class SpendingTrendResponseBuilder {
        private Integer month;
        private Integer year;
        private BigDecimal totalSpending;
        private BigDecimal totalIncome;

        public SpendingTrendResponseBuilder month(Integer month) { this.month = month; return this; }
        public SpendingTrendResponseBuilder year(Integer year) { this.year = year; return this; }
        public SpendingTrendResponseBuilder totalSpending(BigDecimal totalSpending) { this.totalSpending = totalSpending; return this; }
        public SpendingTrendResponseBuilder totalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; return this; }

        public SpendingTrendResponse build() {
            return new SpendingTrendResponse(month, year, totalSpending, totalIncome);
        }
    }
}
