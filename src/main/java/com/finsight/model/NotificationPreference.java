package com.finsight.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    private Boolean budgetAlertsEnabled = true;
    
    @Column(nullable = false)
    private Integer budgetAlertThreshold = 80;
    
    @Column(nullable = false)
    private Boolean alertEmail = true;
    
    @Column(nullable = false)
    private Boolean alertInApp = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertFrequency alertFrequency = AlertFrequency.REAL_TIME;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum AlertFrequency {
        REAL_TIME, DAILY, WEEKLY
    }
    
    public NotificationPreference() {}
    
    public NotificationPreference(User user) {
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Boolean getBudgetAlertsEnabled() { return budgetAlertsEnabled; }
    public void setBudgetAlertsEnabled(Boolean budgetAlertsEnabled) { this.budgetAlertsEnabled = budgetAlertsEnabled; }
    
    public Integer getBudgetAlertThreshold() { return budgetAlertThreshold; }
    public void setBudgetAlertThreshold(Integer budgetAlertThreshold) { this.budgetAlertThreshold = budgetAlertThreshold; }
    
    public Boolean getAlertEmail() { return alertEmail; }
    public void setAlertEmail(Boolean alertEmail) { this.alertEmail = alertEmail; }
    
    public Boolean getAlertInApp() { return alertInApp; }
    public void setAlertInApp(Boolean alertInApp) { this.alertInApp = alertInApp; }
    
    public AlertFrequency getAlertFrequency() { return alertFrequency; }
    public void setAlertFrequency(AlertFrequency alertFrequency) { this.alertFrequency = alertFrequency; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
