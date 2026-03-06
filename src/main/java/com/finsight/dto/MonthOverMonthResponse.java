package com.finsight.dto;

import java.math.BigDecimal;

public class MonthOverMonthResponse {
    private int currentMonth;
    private int currentYear;
    private BigDecimal currentIncome;
    private BigDecimal currentExpense;
    private BigDecimal previousIncome;
    private BigDecimal previousExpense;
    private BigDecimal incomeChange;
    private BigDecimal expenseChange;
    private double incomeChangePercent;
    private double expenseChangePercent;

    public MonthOverMonthResponse() {}

    public int getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(int currentMonth) { this.currentMonth = currentMonth; }
    public int getCurrentYear() { return currentYear; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }
    public BigDecimal getCurrentIncome() { return currentIncome; }
    public void setCurrentIncome(BigDecimal currentIncome) { this.currentIncome = currentIncome; }
    public BigDecimal getCurrentExpense() { return currentExpense; }
    public void setCurrentExpense(BigDecimal currentExpense) { this.currentExpense = currentExpense; }
    public BigDecimal getPreviousIncome() { return previousIncome; }
    public void setPreviousIncome(BigDecimal previousIncome) { this.previousIncome = previousIncome; }
    public BigDecimal getPreviousExpense() { return previousExpense; }
    public void setPreviousExpense(BigDecimal previousExpense) { this.previousExpense = previousExpense; }
    public BigDecimal getIncomeChange() { return incomeChange; }
    public void setIncomeChange(BigDecimal incomeChange) { this.incomeChange = incomeChange; }
    public BigDecimal getExpenseChange() { return expenseChange; }
    public void setExpenseChange(BigDecimal expenseChange) { this.expenseChange = expenseChange; }
    public double getIncomeChangePercent() { return incomeChangePercent; }
    public void setIncomeChangePercent(double incomeChangePercent) { this.incomeChangePercent = incomeChangePercent; }
    public double getExpenseChangePercent() { return expenseChangePercent; }
    public void setExpenseChangePercent(double expenseChangePercent) { this.expenseChangePercent = expenseChangePercent; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final MonthOverMonthResponse r = new MonthOverMonthResponse();
        public Builder currentMonth(int v) { r.currentMonth = v; return this; }
        public Builder currentYear(int v) { r.currentYear = v; return this; }
        public Builder currentIncome(BigDecimal v) { r.currentIncome = v; return this; }
        public Builder currentExpense(BigDecimal v) { r.currentExpense = v; return this; }
        public Builder previousIncome(BigDecimal v) { r.previousIncome = v; return this; }
        public Builder previousExpense(BigDecimal v) { r.previousExpense = v; return this; }
        public Builder incomeChange(BigDecimal v) { r.incomeChange = v; return this; }
        public Builder expenseChange(BigDecimal v) { r.expenseChange = v; return this; }
        public Builder incomeChangePercent(double v) { r.incomeChangePercent = v; return this; }
        public Builder expenseChangePercent(double v) { r.expenseChangePercent = v; return this; }
        public MonthOverMonthResponse build() { return r; }
    }
}
