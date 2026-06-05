package com.finsight.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public OutboxEvent enqueue(String topic, String eventType, String eventKey, Long userId, Object payload) {
        JsonNode payloadNode = objectMapper.valueToTree(payload);
        KafkaEvent event = new KafkaEvent(eventType, userId, payloadNode);
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize event", ex);
        }
        OutboxEvent outboxEvent = new OutboxEvent(
                event.getId(),
                event.getType(),
                topic,
                eventKey,
                json,
                userId
        );
        return outboxEventRepository.save(outboxEvent);
    }
}
