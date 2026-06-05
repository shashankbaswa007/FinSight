package com.finsight.controller;

import com.finsight.dto.NotificationPreferencesResponse;
import com.finsight.dto.NotificationResponse;
import com.finsight.dto.UpdateNotificationPreferencesRequest;
import com.finsight.model.NotificationPreference;
import com.finsight.service.NotificationService;
import com.finsight.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController extends BaseController {
    
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;
    
    public NotificationController(NotificationService notificationService, SecurityUtil securityUtil) {
        this.notificationService = notificationService;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Get all unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> unreadNotifications = notificationService.getUnreadNotifications(userId, pageable)
            .map(NotificationResponse::fromEntity);
        return ResponseEntity.ok(unreadNotifications);
    }
    
    /**
     * Get all notifications (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getAllNotifications(userId, pageable)
            .map(NotificationResponse::fromEntity);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) {
        Long userId = getUserId();
        Long resolvedNotificationId = Objects.requireNonNull(notificationId, "notificationId");
        NotificationResponse response = NotificationResponse.fromEntity(
            notificationService.markAsRead(userId, resolvedNotificationId)
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = getUserId();
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Get notification preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences() {
        Long userId = getUserId();
        NotificationPreference preference = notificationService.getPreferences(userId);
        return ResponseEntity.ok(toResponse(preference));
    }

    /**
     * Update notification preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        Long userId = getUserId();
        NotificationPreference preference = notificationService.updatePreferences(
                userId,
                request.getBudgetAlertsEnabled(),
                request.getBudgetAlertThreshold(),
                request.getAlertEmail(),
                request.getAlertInApp(),
                NotificationPreference.AlertFrequency.valueOf(request.getAlertFrequency())
        );
        return ResponseEntity.ok(toResponse(preference));
    }

    private NotificationPreferencesResponse toResponse(NotificationPreference preference) {
        return new NotificationPreferencesResponse(
                preference.getBudgetAlertsEnabled(),
                preference.getBudgetAlertThreshold(),
                preference.getAlertEmail(),
                preference.getAlertInApp(),
                preference.getAlertFrequency().name()
        );
    }

    private @NonNull Long getUserId() {
        return Objects.requireNonNull(securityUtil.getCurrentUserId(), "userId");
    }
}
