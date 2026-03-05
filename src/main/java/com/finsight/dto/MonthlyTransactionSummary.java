package com.finsight.dto;

import java.math.BigDecimal;

/**
 * DTO for a monthly transaction summary grouped by month/year.
 */
public class MonthlyTransactionSummary {

    private Integer month;
    private Integer year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private long transactionCount;

    public MonthlyTransactionSummary() {}

    public MonthlyTransactionSummary(Integer month, Integer year, BigDecimal totalIncome,
                                    BigDecimal totalExpense, long transactionCount) {
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.transactionCount = transactionCount;
    }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }

    public static MonthlyTransactionSummaryBuilder builder() { return new MonthlyTransactionSummaryBuilder(); }

    public static class MonthlyTransactionSummaryBuilder {
        private Integer month;
        private Integer year;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private long transactionCount;

        public MonthlyTransactionSummaryBuilder month(Integer month) { this.month = month; return this; }
        public MonthlyTransactionSummaryBuilder year(Integer year) { this.year = year; return this; }
        public MonthlyTransactionSummaryBuilder totalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; return this; }
        public MonthlyTransactionSummaryBuilder totalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; return this; }
        public MonthlyTransactionSummaryBuilder transactionCount(long transactionCount) { this.transactionCount = transactionCount; return this; }

        public MonthlyTransactionSummary build() {
            return new MonthlyTransactionSummary(month, year, totalIncome, totalExpense, transactionCount);
        }
    }
}
