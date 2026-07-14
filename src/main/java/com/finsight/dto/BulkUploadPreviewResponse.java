package com.finsight.dto;

import java.util.List;

public class BulkUploadPreviewResponse {
    private List<BulkUploadRowDto> validRows;
    private List<BulkUploadErrorDto> errorRows;
    private int totalRows;
    private int validCount;
    private int errorCount;
    private List<BulkUploadRowDto> duplicateRows;
    private int duplicateCount;

    public BulkUploadPreviewResponse() {}

    public BulkUploadPreviewResponse(List<BulkUploadRowDto> validRows, List<BulkUploadErrorDto> errorRows, 
                                     int totalRows, int validCount, int errorCount,
                                     List<BulkUploadRowDto> duplicateRows, int duplicateCount) {
        this.validRows = validRows;
        this.errorRows = errorRows;
        this.totalRows = totalRows;
        this.validCount = validCount;
        this.errorCount = errorCount;
        this.duplicateRows = duplicateRows;
        this.duplicateCount = duplicateCount;
    }

    public List<BulkUploadRowDto> getValidRows() { return validRows; }
    public void setValidRows(List<BulkUploadRowDto> validRows) { this.validRows = validRows; }
    public List<BulkUploadErrorDto> getErrorRows() { return errorRows; }
    public void setErrorRows(List<BulkUploadErrorDto> errorRows) { this.errorRows = errorRows; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getValidCount() { return validCount; }
    public void setValidCount(int validCount) { this.validCount = validCount; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public List<BulkUploadRowDto> getDuplicateRows() { return duplicateRows; }
    public void setDuplicateRows(List<BulkUploadRowDto> duplicateRows) { this.duplicateRows = duplicateRows; }
    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }
}
