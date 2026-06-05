package com.finsight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finsight.messaging.KafkaEvent;
import com.finsight.messaging.KafkaTopics;
import com.finsight.messaging.OutboxService;
import com.finsight.model.Webhook;
import com.finsight.model.WebhookDelivery;
import com.finsight.repository.WebhookDeliveryRepository;
import com.finsight.repository.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class WebhookDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookDeliveryService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final OutboxService outboxService;
    private final KafkaTopics kafkaTopics;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${finsight.webhook.delivery.timeout-seconds:10}")
    private int timeoutSeconds;

    public WebhookDeliveryService(WebhookRepository webhookRepository,
                                  WebhookDeliveryRepository webhookDeliveryRepository,
                                  OutboxService outboxService,
                                  KafkaTopics kafkaTopics,
                                  ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.outboxService = outboxService;
        this.kafkaTopics = kafkaTopics;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public int enqueueDeliveries(KafkaEvent event) {
        if (event == null || event.getType() == null) {
            return 0;
        }

        Long targetWebhookId = extractTargetWebhookId(event.getPayload());
        List<Webhook> webhooks = resolveWebhooks(event.getUserId(), targetWebhookId);
        if (webhooks.isEmpty()) {
            return 0;
        }

        String payloadJson = buildEnvelopePayload(event);
        if (payloadJson == null) {
            return 0;
        }

        int created = 0;
        for (Webhook webhook : webhooks) {
            if (!webhook.getActive()) {
                continue;
            }
            if (!supportsEventType(webhook, event.getType())) {
                continue;
            }
            WebhookDelivery delivery = new WebhookDelivery(webhook, event.getType(), payloadJson);
            delivery.setNextRetry(LocalDateTime.now());
            webhookDeliveryRepository.save(delivery);
            created++;
        }
        return created;
    }

    public void processPendingDeliveries() {
        List<WebhookDelivery> deliveries = webhookDeliveryRepository
                .findTop50ByStatusAndNextRetryBeforeOrderByNextRetryAsc(
                        WebhookDelivery.Status.PENDING,
                        LocalDateTime.now()
                );
        for (WebhookDelivery delivery : deliveries) {
            attemptDelivery(delivery);
        }
    }

    private void attemptDelivery(WebhookDelivery delivery) {
        Webhook webhook = delivery.getWebhook();
        if (webhook == null || !Boolean.TRUE.equals(webhook.getActive())) {
            markFailed(delivery, "Webhook inactive", null);
            return;
        }

        int attempt = delivery.getAttemptCount() + 1;
        delivery.setAttemptCount(attempt);

        try {
            String signature = generateSignature(webhook.getSecret(), delivery.getPayload());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhook.getUrl()))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Signature", signature)
                    .header("X-Event-Type", delivery.getEventType())
                    .header("X-Delivery-Id", String.valueOf(delivery.getId()))
                    .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayload()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            delivery.setResponseStatusCode(response.statusCode());
            delivery.setResponseBody(response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.setStatus(WebhookDelivery.Status.DELIVERED);
                delivery.setDeliveredAt(LocalDateTime.now());
                delivery.setNextRetry(null);
                webhookDeliveryRepository.save(delivery);
                return;
            }

            handleRetryOrFail(delivery, webhook, "HTTP " + response.statusCode());
        } catch (IOException | GeneralSecurityException | RuntimeException ex) {
            delivery.setResponseBody(ex.getMessage());
            handleRetryOrFail(delivery, webhook, ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            delivery.setResponseBody(ex.getMessage());
            handleRetryOrFail(delivery, webhook, ex.getMessage());
        }
    }

    private void handleRetryOrFail(WebhookDelivery delivery, Webhook webhook, String error) {
        int attempt = delivery.getAttemptCount();
        Integer retryCount = webhook.getRetryCount();
        int maxAttempts = retryCount == null ? 0 : retryCount;

        if (attempt < maxAttempts) {
            long delaySeconds = Math.max(1, (long) Math.pow(2, attempt));
            delivery.setStatus(WebhookDelivery.Status.PENDING);
            delivery.setNextRetry(LocalDateTime.now().plusSeconds(delaySeconds));
            webhookDeliveryRepository.save(delivery);
            logger.warn("Webhook delivery {} failed (attempt {}/{}): {}", delivery.getId(), attempt, maxAttempts, error);
            return;
        }

        markFailed(delivery, error, webhook);
    }

    private void markFailed(WebhookDelivery delivery, String error, Webhook webhook) {
        delivery.setStatus(WebhookDelivery.Status.FAILED);
        delivery.setNextRetry(null);
        if (delivery.getResponseBody() == null) {
            delivery.setResponseBody(error);
        }
        webhookDeliveryRepository.save(delivery);
        logger.error("Webhook delivery {} failed permanently: {}", delivery.getId(), error);

        if (webhook != null && webhook.getUser() != null) {
            Map<String, Object> dlqPayload = new HashMap<>();
            dlqPayload.put("deliveryId", delivery.getId());
            dlqPayload.put("webhookId", webhook.getId());
            dlqPayload.put("eventType", delivery.getEventType());
            dlqPayload.put("attempts", delivery.getAttemptCount());
            dlqPayload.put("responseStatus", delivery.getResponseStatusCode());
            dlqPayload.put("responseBody", delivery.getResponseBody());

            outboxService.enqueue(
                    kafkaTopics.getDlq(),
                    "webhook.delivery.failed",
                    String.valueOf(webhook.getId()),
                    webhook.getUser().getId(),
                    dlqPayload
            );
        }
    }

    private List<Webhook> resolveWebhooks(Long userId, Long targetWebhookId) {
        List<Webhook> webhooks = new ArrayList<>();
        if (targetWebhookId != null) {
            Optional<Webhook> webhook = webhookRepository.findById(targetWebhookId);
            webhook.ifPresent(webhooks::add);
            return webhooks;
        }
        if (userId == null) {
            return webhooks;
        }
        return webhookRepository.findByUserIdAndActive(userId);
    }

    private boolean supportsEventType(Webhook webhook, String eventType) {
        String types = webhook.getEventTypes();
        if (types == null || types.isBlank()) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(types);
            if (node.isArray()) {
                for (JsonNode entry : node) {
                    String value = entry.asText();
                    if ("*".equals(value) || "all".equalsIgnoreCase(value) || eventType.equals(value)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (IOException ignored) {
            // fallback to substring match
        }
        return types.contains(eventType) || "*".equals(types) || "all".equalsIgnoreCase(types.trim());
    }

    private String buildEnvelopePayload(KafkaEvent event) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("id", event.getId());
            envelope.put("type", event.getType());
            envelope.put("timestamp", event.getTimestamp());
            if (event.getUserId() != null) {
                envelope.put("userId", event.getUserId());
            }
            JsonNode payloadNode = sanitizePayload(event.getPayload());
            if (payloadNode != null) {
                envelope.set("data", payloadNode);
            }
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            logger.warn("Failed to build webhook payload", ex);
            return null;
        }
    }

    private JsonNode sanitizePayload(JsonNode payloadNode) {
        if (payloadNode == null) {
            return null;
        }
        if (payloadNode.isObject() && payloadNode.has("targetWebhookId")) {
            ObjectNode copy = payloadNode.deepCopy();
            copy.remove("targetWebhookId");
            return copy;
        }
        return payloadNode;
    }

    private Long extractTargetWebhookId(JsonNode payloadNode) {
        if (payloadNode == null || !payloadNode.has("targetWebhookId")) {
            return null;
        }
        JsonNode idNode = payloadNode.get("targetWebhookId");
        if (idNode != null && idNode.canConvertToLong()) {
            return idNode.asLong();
        }
        return null;
    }

    private String generateSignature(String secret, String payload) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), 0, secret.getBytes().length, HMAC_SHA256);
        mac.init(keySpec);
        byte[] signature = mac.doFinal(payload.getBytes());
        return java.util.Base64.getEncoder().encodeToString(signature);
    }
}
