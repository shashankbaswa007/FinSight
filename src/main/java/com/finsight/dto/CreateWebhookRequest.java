package com.finsight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public class CreateWebhookRequest {
    
    @NotBlank(message = "URL is required")
    private String url;
    
    @NotEmpty(message = "Event types are required")
    private String eventTypes; // JSON array string
    
    @Positive(message = "Retry count must be positive")
    private Integer retryCount = 3;
    
    public CreateWebhookRequest() {}
    
    public CreateWebhookRequest(String url, String eventTypes) {
        this.url = url;
        this.eventTypes = eventTypes;
    }
    
    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getEventTypes() { return eventTypes; }
    public void setEventTypes(String eventTypes) { this.eventTypes = eventTypes; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
