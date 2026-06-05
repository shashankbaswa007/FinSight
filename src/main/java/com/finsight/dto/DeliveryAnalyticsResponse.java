package com.finsight.dto;

import java.time.LocalDateTime;

public class DeliveryAnalyticsResponse {

    private LocalDateTime start;
    private LocalDateTime end;
    private DeliveryChannelMetricsResponse notificationEmail;
    private DeliveryChannelMetricsResponse webhook;

    public DeliveryAnalyticsResponse() {}

    public DeliveryAnalyticsResponse(LocalDateTime start, LocalDateTime end,
                                    DeliveryChannelMetricsResponse notificationEmail,
                                    DeliveryChannelMetricsResponse webhook) {
        this.start = start;
        this.end = end;
        this.notificationEmail = notificationEmail;
        this.webhook = webhook;
    }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public DeliveryChannelMetricsResponse getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(DeliveryChannelMetricsResponse notificationEmail) { this.notificationEmail = notificationEmail; }

    public DeliveryChannelMetricsResponse getWebhook() { return webhook; }
    public void setWebhook(DeliveryChannelMetricsResponse webhook) { this.webhook = webhook; }

    public static DeliveryAnalyticsResponseBuilder builder() { return new DeliveryAnalyticsResponseBuilder(); }

    public static class DeliveryAnalyticsResponseBuilder {
        private LocalDateTime start;
        private LocalDateTime end;
        private DeliveryChannelMetricsResponse notificationEmail;
        private DeliveryChannelMetricsResponse webhook;

        public DeliveryAnalyticsResponseBuilder start(LocalDateTime start) { this.start = start; return this; }
        public DeliveryAnalyticsResponseBuilder end(LocalDateTime end) { this.end = end; return this; }
        public DeliveryAnalyticsResponseBuilder notificationEmail(DeliveryChannelMetricsResponse notificationEmail) { this.notificationEmail = notificationEmail; return this; }
        public DeliveryAnalyticsResponseBuilder webhook(DeliveryChannelMetricsResponse webhook) { this.webhook = webhook; return this; }

        public DeliveryAnalyticsResponse build() {
            return new DeliveryAnalyticsResponse(start, end, notificationEmail, webhook);
        }
    }
}
