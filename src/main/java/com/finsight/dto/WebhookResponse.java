package com.finsight.dto;

import java.time.LocalDateTime;

public class WebhookResponse {
    private Long id;
    private String url;
    private String eventTypes;
    private Boolean active;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public WebhookResponse() {}
    
    public WebhookResponse(Long id, String url, String eventTypes, Boolean active, 
                          Integer retryCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.url = url;
        this.eventTypes = eventTypes;
        this.active = active;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static WebhookResponse fromEntity(com.finsight.model.Webhook webhook) {
        return new WebhookResponse(
            webhook.getId(),
            webhook.getUrl(),
            webhook.getEventTypes(),
            webhook.getActive(),
            webhook.getRetryCount(),
            webhook.getCreatedAt(),
            webhook.getUpdatedAt()
        );
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getEventTypes() { return eventTypes; }
    public void setEventTypes(String eventTypes) { this.eventTypes = eventTypes; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
