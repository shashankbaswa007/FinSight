package com.finsight.dto;

import java.time.LocalDateTime;

public class ProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public ProfileResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ProfileResponse r = new ProfileResponse();
        public Builder userId(Long v) { r.userId = v; return this; }
        public Builder name(String v) { r.name = v; return this; }
        public Builder email(String v) { r.email = v; return this; }
        public Builder role(String v) { r.role = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public ProfileResponse build() { return r; }
    }
}
