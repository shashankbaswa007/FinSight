package com.finsight.dto;

import java.math.BigDecimal;

/**
 * DTO reporting the status of a budget: spent amount vs. limit and whether it is exceeded.
 */
public class BudgetStatusResponse {

    private Long budgetId;
    private String categoryName;
    private BigDecimal monthlyLimit;
    private BigDecimal amountSpent;
    private BigDecimal remaining;
    private boolean exceeded;
    private Integer month;
    private Integer year;

    public BudgetStatusResponse() {}

    public BudgetStatusResponse(Long budgetId, String categoryName, BigDecimal monthlyLimit,
                                BigDecimal amountSpent, BigDecimal remaining, boolean exceeded,
                                Integer month, Integer year) {
        this.budgetId = budgetId;
        this.categoryName = categoryName;
        this.monthlyLimit = monthlyLimit;
        this.amountSpent = amountSpent;
        this.remaining = remaining;
        this.exceeded = exceeded;
        this.month = month;
        this.year = year;
    }

    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public BigDecimal getAmountSpent() { return amountSpent; }
    public void setAmountSpent(BigDecimal amountSpent) { this.amountSpent = amountSpent; }

    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }

    public boolean isExceeded() { return exceeded; }
    public void setExceeded(boolean exceeded) { this.exceeded = exceeded; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public static BudgetStatusResponseBuilder builder() { return new BudgetStatusResponseBuilder(); }

    public static class BudgetStatusResponseBuilder {
        private Long budgetId;
        private String categoryName;
        private BigDecimal monthlyLimit;
        private BigDecimal amountSpent;
        private BigDecimal remaining;
        private boolean exceeded;
        private Integer month;
        private Integer year;

        public BudgetStatusResponseBuilder budgetId(Long budgetId) { this.budgetId = budgetId; return this; }
        public BudgetStatusResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public BudgetStatusResponseBuilder monthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; return this; }
        public BudgetStatusResponseBuilder amountSpent(BigDecimal amountSpent) { this.amountSpent = amountSpent; return this; }
        public BudgetStatusResponseBuilder remaining(BigDecimal remaining) { this.remaining = remaining; return this; }
        public BudgetStatusResponseBuilder exceeded(boolean exceeded) { this.exceeded = exceeded; return this; }
        public BudgetStatusResponseBuilder month(Integer month) { this.month = month; return this; }
        public BudgetStatusResponseBuilder year(Integer year) { this.year = year; return this; }

        public BudgetStatusResponse build() {
            return new BudgetStatusResponse(budgetId, categoryName, monthlyLimit, amountSpent, remaining, exceeded, month, year);
        }
    }
}
