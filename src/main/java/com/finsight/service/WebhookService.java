package com.finsight.service;

import com.finsight.messaging.KafkaTopics;
import com.finsight.messaging.OutboxService;
import com.finsight.model.User;
import com.finsight.model.Webhook;
import com.finsight.repository.UserRepository;
import com.finsight.repository.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class WebhookService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    
    private final WebhookRepository webhookRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
    private final KafkaTopics kafkaTopics;
    
    public WebhookService(WebhookRepository webhookRepository,
                          UserRepository userRepository,
                          OutboxService outboxService,
                          KafkaTopics kafkaTopics) {
        this.webhookRepository = webhookRepository;
        this.userRepository = userRepository;
        this.outboxService = outboxService;
        this.kafkaTopics = kafkaTopics;
    }
    
    /**
     * Create webhook for user
     */
    public Webhook createWebhook(@NonNull Long userId, String url, String eventTypes, Integer retryCount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String secret = generateSecret();
        Webhook webhook = new Webhook(user, url, eventTypes, secret);
        if (retryCount != null) {
            webhook.setRetryCount(retryCount);
        }
        
        Webhook saved = webhookRepository.save(webhook);
        logger.info("Created webhook for user {} at URL {}", userId, url);
        return saved;
    }
    
    /**
     * Get all webhooks for user
     */
    @Transactional(readOnly = true)
    public List<Webhook> getUserWebhooks(@NonNull Long userId) {
        return webhookRepository.findByUserId(userId);
    }
    
    /**
     * Get active webhooks for user
     */
    @Transactional(readOnly = true)
    public List<Webhook> getActiveWebhooks(@NonNull Long userId) {
        return webhookRepository.findByUserIdAndActive(userId);
    }
    
    /**
     * Update webhook
     */
    public Webhook updateWebhook(@NonNull Long userId, @NonNull Long webhookId, String url, String eventTypes, Boolean active) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new RuntimeException("Webhook not found"));

        Long ownerId = Objects.requireNonNull(webhook.getUser().getId(), "webhook.userId");
        if (!ownerId.equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        if (url != null) webhook.setUrl(url);
        if (eventTypes != null) webhook.setEventTypes(eventTypes);
        if (active != null) webhook.setActive(active);
        
        return webhookRepository.save(webhook);
    }
    
    /**
     * Delete webhook
     */
    public void deleteWebhook(@NonNull Long userId, @NonNull Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new RuntimeException("Webhook not found"));

        Long ownerId = Objects.requireNonNull(webhook.getUser().getId(), "webhook.userId");
        if (!ownerId.equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        webhookRepository.delete(webhook);
        logger.info("Deleted webhook {} for user {}", webhookId, userId);
    }
    
    /**
     * Enqueue webhook event for async delivery
     */
    public void sendWebhookEvent(@NonNull Long webhookId, String eventType, Map<String, Object> payload) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new RuntimeException("Webhook not found"));
        
        if (!webhook.getActive()) {
            logger.warn("Attempted to send to inactive webhook {}", webhookId);
            return;
        }

        Map<String, Object> eventPayload = new HashMap<>();
        if (payload != null) {
            eventPayload.putAll(payload);
        }
        eventPayload.put("targetWebhookId", webhookId);

        Long ownerId = Objects.requireNonNull(webhook.getUser().getId(), "webhook.userId");
        outboxService.enqueue(
                kafkaTopics.getWebhooks(),
                eventType,
                String.valueOf(webhookId),
            ownerId,
                eventPayload
        );
    }
    
    /**
     * Generate random secret for webhook
     */
    private String generateSecret() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
}
