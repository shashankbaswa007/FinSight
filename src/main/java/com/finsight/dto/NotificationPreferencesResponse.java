package com.finsight.dto;

public class NotificationPreferencesResponse {

    private Boolean budgetAlertsEnabled;
    private Integer budgetAlertThreshold;
    private Boolean alertEmail;
    private Boolean alertInApp;
    private String alertFrequency;

    public NotificationPreferencesResponse() {}

    public NotificationPreferencesResponse(Boolean budgetAlertsEnabled,
                                           Integer budgetAlertThreshold,
                                           Boolean alertEmail,
                                           Boolean alertInApp,
                                           String alertFrequency) {
        this.budgetAlertsEnabled = budgetAlertsEnabled;
        this.budgetAlertThreshold = budgetAlertThreshold;
        this.alertEmail = alertEmail;
        this.alertInApp = alertInApp;
        this.alertFrequency = alertFrequency;
    }

    public Boolean getBudgetAlertsEnabled() { return budgetAlertsEnabled; }
    public void setBudgetAlertsEnabled(Boolean budgetAlertsEnabled) { this.budgetAlertsEnabled = budgetAlertsEnabled; }

    public Integer getBudgetAlertThreshold() { return budgetAlertThreshold; }
    public void setBudgetAlertThreshold(Integer budgetAlertThreshold) { this.budgetAlertThreshold = budgetAlertThreshold; }

    public Boolean getAlertEmail() { return alertEmail; }
    public void setAlertEmail(Boolean alertEmail) { this.alertEmail = alertEmail; }

    public Boolean getAlertInApp() { return alertInApp; }
    public void setAlertInApp(Boolean alertInApp) { this.alertInApp = alertInApp; }

    public String getAlertFrequency() { return alertFrequency; }
    public void setAlertFrequency(String alertFrequency) { this.alertFrequency = alertFrequency; }
}
