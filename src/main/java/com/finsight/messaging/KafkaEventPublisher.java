package com.finsight.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(@NonNull OutboxEvent event) {
        String topic = Objects.requireNonNull(event.getTopic(), "topic");
        String eventKey = Objects.requireNonNull(event.getEventKey(), "eventKey");
        String payload = Objects.requireNonNull(event.getPayload(), "payload");
        try {
            kafkaTemplate.send(topic, eventKey, payload).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka publish interrupted", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Kafka publish failed", ex.getCause());
        }
    }
}
