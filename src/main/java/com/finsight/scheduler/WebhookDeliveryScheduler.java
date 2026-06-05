package com.finsight.scheduler;

import com.finsight.service.WebhookDeliveryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "finsight.webhook.delivery", name = "enabled", havingValue = "true")
public class WebhookDeliveryScheduler {

    private final WebhookDeliveryService webhookDeliveryService;

    public WebhookDeliveryScheduler(WebhookDeliveryService webhookDeliveryService) {
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Scheduled(fixedDelayString = "${finsight.webhook.delivery.poll-interval-ms:5000}")
    public void processPendingDeliveries() {
        webhookDeliveryService.processPendingDeliveries();
    }
}
