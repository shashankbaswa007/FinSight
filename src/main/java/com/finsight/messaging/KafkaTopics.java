package com.finsight.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "finsight.kafka.topics")
public class KafkaTopics {

    private String reconciliation;
    private String notifications;
    private String webhooks;
    private String digests;
    private String dlq;

    public String getReconciliation() { return reconciliation; }
    public void setReconciliation(String reconciliation) { this.reconciliation = reconciliation; }

    public String getNotifications() { return notifications; }
    public void setNotifications(String notifications) { this.notifications = notifications; }

    public String getWebhooks() { return webhooks; }
    public void setWebhooks(String webhooks) { this.webhooks = webhooks; }

    public String getDigests() { return digests; }
    public void setDigests(String digests) { this.digests = digests; }

    public String getDlq() { return dlq; }
    public void setDlq(String dlq) { this.dlq = dlq; }
}
