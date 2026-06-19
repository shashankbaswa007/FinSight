package com.finsight.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaEventPublisher {

    public KafkaEventPublisher() {
    }

    public void publish(@NonNull OutboxEvent event) {
        String topic = Objects.requireNonNull(event.getTopic(), "topic");
        String eventKey = Objects.requireNonNull(event.getEventKey(), "eventKey");
        String payload = Objects.requireNonNull(event.getPayload(), "payload");
        // BYPASS FOR UI DEMO WITHOUT KAFKA
        System.out.println("Mock published to Kafka: " + topic + " - " + eventKey);
    }
}
