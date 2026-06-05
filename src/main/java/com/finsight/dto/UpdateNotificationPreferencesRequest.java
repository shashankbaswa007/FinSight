package com.finsight.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UpdateNotificationPreferencesRequest {

    @NotNull(message = "Budget alerts enabled flag is required")
    private Boolean budgetAlertsEnabled;

    @NotNull(message = "Budget alert threshold is required")
    @Min(value = 1, message = "Budget alert threshold must be at least 1")
    @Max(value = 100, message = "Budget alert threshold must be at most 100")
    private Integer budgetAlertThreshold;

    @NotNull(message = "Alert email flag is required")
    private Boolean alertEmail;

    @NotNull(message = "Alert in-app flag is required")
    private Boolean alertInApp;

    @NotNull(message = "Alert frequency is required")
    @Pattern(regexp = "REAL_TIME|DAILY|WEEKLY", message = "Alert frequency must be REAL_TIME, DAILY, or WEEKLY")
    private String alertFrequency;

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
