package com.finsight.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import com.finsight.service.WebhookDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "finsight.kafka.webhooks", name = "enabled", havingValue = "true")
public class WebhookEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WebhookEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final WebhookDeliveryService webhookDeliveryService;

    public WebhookEventConsumer(ObjectMapper objectMapper, WebhookDeliveryService webhookDeliveryService) {
        this.objectMapper = objectMapper;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @KafkaListener(topics = "#{@kafkaTopics.webhooks}")
    public void onMessage(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            int created = webhookDeliveryService.enqueueDeliveries(event);
            if (created > 0) {
                logger.info("Enqueued {} webhook deliveries for event {}", created, event.getId());
            }
        } catch (IOException ex) {
            logger.warn("Failed to process webhook event message", ex);
        }
    }
}
