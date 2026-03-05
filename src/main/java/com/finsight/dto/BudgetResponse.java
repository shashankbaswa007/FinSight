package com.finsight.dto;

import java.math.BigDecimal;

/**
 * DTO for budget response data.
 */
public class BudgetResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal monthlyLimit;
    private Integer month;
    private Integer year;

    public BudgetResponse() {}

    public BudgetResponse(Long id, Long categoryId, String categoryName, BigDecimal monthlyLimit, Integer month, Integer year) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.monthlyLimit = monthlyLimit;
        this.month = month;
        this.year = year;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public static BudgetResponseBuilder builder() { return new BudgetResponseBuilder(); }

    public static class BudgetResponseBuilder {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private BigDecimal monthlyLimit;
        private Integer month;
        private Integer year;

        public BudgetResponseBuilder id(Long id) { this.id = id; return this; }
        public BudgetResponseBuilder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public BudgetResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public BudgetResponseBuilder monthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; return this; }
        public BudgetResponseBuilder month(Integer month) { this.month = month; return this; }
        public BudgetResponseBuilder year(Integer year) { this.year = year; return this; }

        public BudgetResponse build() {
            return new BudgetResponse(id, categoryId, categoryName, monthlyLimit, month, year);
        }
    }
}
