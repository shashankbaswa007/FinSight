package com.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BulkUploadRowDto {
    private int rowNumber;
    private BigDecimal amount;
    private String type;
    private String categoryName;
    private Long categoryId;
    private String description;
    private LocalDate date;

    public BulkUploadRowDto() {}

    public BulkUploadRowDto(int rowNumber, BigDecimal amount, String type, String categoryName, Long categoryId, String description, LocalDate date) {
        this.rowNumber = rowNumber;
        this.amount = amount;
        this.type = type;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.description = description;
        this.date = date;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
