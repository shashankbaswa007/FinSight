package com.finsight.dto;

import java.math.BigDecimal;

/**
 * DTO for a monthly financial summary: total income, expenses, and net savings.
 */
public class MonthlySummaryResponse {

    private Integer month;
    private Integer year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private BigDecimal incomeExpenseRatio;

    public MonthlySummaryResponse() {}

    public MonthlySummaryResponse(Integer month, Integer year, BigDecimal totalIncome,
                                  BigDecimal totalExpense, BigDecimal netSavings, BigDecimal incomeExpenseRatio) {
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netSavings = netSavings;
        this.incomeExpenseRatio = incomeExpenseRatio;
    }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getNetSavings() { return netSavings; }
    public void setNetSavings(BigDecimal netSavings) { this.netSavings = netSavings; }

    public BigDecimal getIncomeExpenseRatio() { return incomeExpenseRatio; }
    public void setIncomeExpenseRatio(BigDecimal incomeExpenseRatio) { this.incomeExpenseRatio = incomeExpenseRatio; }

    public static MonthlySummaryResponseBuilder builder() { return new MonthlySummaryResponseBuilder(); }

    public static class MonthlySummaryResponseBuilder {
        private Integer month;
        private Integer year;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netSavings;
        private BigDecimal incomeExpenseRatio;

        public MonthlySummaryResponseBuilder month(Integer month) { this.month = month; return this; }
        public MonthlySummaryResponseBuilder year(Integer year) { this.year = year; return this; }
        public MonthlySummaryResponseBuilder totalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; return this; }
        public MonthlySummaryResponseBuilder totalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; return this; }
        public MonthlySummaryResponseBuilder netSavings(BigDecimal netSavings) { this.netSavings = netSavings; return this; }
        public MonthlySummaryResponseBuilder incomeExpenseRatio(BigDecimal incomeExpenseRatio) { this.incomeExpenseRatio = incomeExpenseRatio; return this; }

        public MonthlySummaryResponse build() {
            return new MonthlySummaryResponse(month, year, totalIncome, totalExpense, netSavings, incomeExpenseRatio);
        }
    }
}
