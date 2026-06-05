package com.finsight.dto;

public class ImportResponse {
    private Integer count;
    private String message;
    
    public ImportResponse() {}
    
    public ImportResponse(Integer count, String message) {
        this.count = count;
        this.message = message;
    }
    
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
