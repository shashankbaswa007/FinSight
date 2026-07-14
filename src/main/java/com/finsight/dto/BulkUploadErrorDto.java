package com.finsight.dto;

import java.util.List;

public class BulkUploadErrorDto {
    private int rowNumber;
    private String rawData;
    private List<String> errors;

    public BulkUploadErrorDto() {}

    public BulkUploadErrorDto(int rowNumber, String rawData, List<String> errors) {
        this.rowNumber = rowNumber;
        this.rawData = rawData;
        this.errors = errors;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
