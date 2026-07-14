package com.finsight.dto;

public class BulkUploadCommitResponse {
    private int createdCount;
    private int skippedCount;
    private String message;

    public BulkUploadCommitResponse() {}

    public BulkUploadCommitResponse(int createdCount, int skippedCount, String message) {
        this.createdCount = createdCount;
        this.skippedCount = skippedCount;
        this.message = message;
    }

    public int getCreatedCount() { return createdCount; }
    public void setCreatedCount(int createdCount) { this.createdCount = createdCount; }
    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
