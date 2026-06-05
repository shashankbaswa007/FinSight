package com.finsight.dto;

public class ReconciliationScheduleSettingsResponse {

    private boolean globalEnabled;
    private String cron;
    private boolean enabled;

    public ReconciliationScheduleSettingsResponse() {}

    public ReconciliationScheduleSettingsResponse(boolean globalEnabled, String cron, boolean enabled) {
        this.globalEnabled = globalEnabled;
        this.cron = cron;
        this.enabled = enabled;
    }

    public boolean isGlobalEnabled() { return globalEnabled; }
    public void setGlobalEnabled(boolean globalEnabled) { this.globalEnabled = globalEnabled; }

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
