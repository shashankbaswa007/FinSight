package com.finsight.dto;

import java.math.BigDecimal;

public class CategoryTrendResponse {

    private int month;
    private int year;
    private String categoryName;
    private BigDecimal totalAmount;

    public CategoryTrendResponse() {}

    public CategoryTrendResponse(int month, int year, String categoryName, BigDecimal totalAmount) {
        this.month = month;
        this.year = year;
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public static CategoryTrendResponseBuilder builder() { return new CategoryTrendResponseBuilder(); }

    public static class CategoryTrendResponseBuilder {
        private int month;
        private int year;
        private String categoryName;
        private BigDecimal totalAmount;

        public CategoryTrendResponseBuilder month(int month) { this.month = month; return this; }
        public CategoryTrendResponseBuilder year(int year) { this.year = year; return this; }
        public CategoryTrendResponseBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public CategoryTrendResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }

        public CategoryTrendResponse build() { return new CategoryTrendResponse(month, year, categoryName, totalAmount); }
    }
}
