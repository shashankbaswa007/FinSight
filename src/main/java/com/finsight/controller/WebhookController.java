package com.finsight.controller;

import com.finsight.dto.CreateWebhookRequest;
import com.finsight.dto.WebhookResponse;
import com.finsight.model.Webhook;
import com.finsight.service.WebhookService;
import com.finsight.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController extends BaseController {
    
    private final WebhookService webhookService;
    private final SecurityUtil securityUtil;
    
    public WebhookController(WebhookService webhookService, SecurityUtil securityUtil) {
        this.webhookService = webhookService;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Create webhook
     */
    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(@Valid @RequestBody CreateWebhookRequest request) {
        Long userId = getUserId();
        Webhook webhook = webhookService.createWebhook(userId, request.getUrl(), 
            request.getEventTypes(), request.getRetryCount());
        return ResponseEntity.status(HttpStatus.CREATED).body(WebhookResponse.fromEntity(webhook));
    }
    
    /**
     * Get all webhooks for user
     */
    @GetMapping
    public ResponseEntity<List<WebhookResponse>> getUserWebhooks() {
        Long userId = getUserId();
        List<WebhookResponse> webhooks = webhookService.getUserWebhooks(userId)
            .stream()
            .map(WebhookResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(webhooks);
    }
    
    /**
     * Get webhook by ID
     */
    @GetMapping("/{webhookId}")
    public ResponseEntity<WebhookResponse> getWebhook(@PathVariable Long webhookId) {
        Long userId = getUserId();
        Long resolvedWebhookId = Objects.requireNonNull(webhookId, "webhookId");
        Webhook webhook = webhookService.getUserWebhooks(userId)
            .stream()
            .filter(w -> w.getId().equals(resolvedWebhookId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Webhook not found"));
        return ResponseEntity.ok(WebhookResponse.fromEntity(webhook));
    }
    
    /**
     * Update webhook
     */
    @PutMapping("/{webhookId}")
    public ResponseEntity<WebhookResponse> updateWebhook(
            @PathVariable Long webhookId,
            @RequestBody CreateWebhookRequest request) {
        Long userId = getUserId();
        Long resolvedWebhookId = Objects.requireNonNull(webhookId, "webhookId");
        Webhook webhook = webhookService.updateWebhook(userId, resolvedWebhookId, 
            request.getUrl(), request.getEventTypes(), null);
        return ResponseEntity.ok(WebhookResponse.fromEntity(webhook));
    }
    
    /**
     * Delete webhook
     */
    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long webhookId) {
        Long userId = getUserId();
        Long resolvedWebhookId = Objects.requireNonNull(webhookId, "webhookId");
        webhookService.deleteWebhook(userId, resolvedWebhookId);
        return ResponseEntity.noContent().build();
    }

    private @NonNull Long getUserId() {
        return Objects.requireNonNull(securityUtil.getCurrentUserId(), "userId");
    }
}
