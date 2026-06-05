package com.finsight.messaging;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public class KafkaEvent {

    private String id;
    private String type;
    private String version;
    private String timestamp;
    private Long userId;
    private JsonNode payload;

    public KafkaEvent() {}

    public KafkaEvent(String type, Long userId, JsonNode payload) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.version = "1";
        this.timestamp = Instant.now().toString();
        this.userId = userId;
        this.payload = payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}
