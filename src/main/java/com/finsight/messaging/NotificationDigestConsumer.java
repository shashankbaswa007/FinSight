package com.finsight.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.service.NotificationDigestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "finsight.kafka.digests", name = "enabled", havingValue = "true")
public class NotificationDigestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDigestConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationDigestService digestService;

    public NotificationDigestConsumer(ObjectMapper objectMapper, NotificationDigestService digestService) {
        this.objectMapper = objectMapper;
        this.digestService = digestService;
    }

    @KafkaListener(topics = "#{@kafkaTopics.digests}")
    public void onMessage(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            digestService.handleDigestEvent(event);
        } catch (IOException ex) {
            logger.warn("Failed to parse notification digest message", ex);
        } catch (RuntimeException ex) {
            logger.error("Failed to process notification digest message", ex);
        }
    }
}
