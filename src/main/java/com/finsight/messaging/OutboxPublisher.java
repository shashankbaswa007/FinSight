package com.finsight.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "finsight.kafka.outbox", name = "enabled", havingValue = "true")
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${finsight.kafka.outbox.max-attempts:5}")
    private int maxAttempts;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           KafkaEventPublisher kafkaEventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Scheduled(fixedDelayString = "${finsight.kafka.outbox.poll-interval-ms:5000}")
    public void publishPending() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop50ByStatusOrderByCreatedAtAsc(OutboxEvent.Status.PENDING);
        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            try {
                kafkaEventPublisher.publish(Objects.requireNonNull(event, "event"));
                event.setStatus(OutboxEvent.Status.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                event.setLastError(null);
                outboxEventRepository.save(event);
                logger.info("Published outbox event {}", event.getEventId());
            } catch (Exception ex) {
                int attempts = event.getAttempts() + 1;
                event.setAttempts(attempts);
                event.setLastError(ex.getMessage());
                if (attempts >= maxAttempts) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                }
                outboxEventRepository.save(event);
                logger.warn("Failed to publish outbox event {} (attempt {})", event.getEventId(), attempts);
            }
        }
    }
}
