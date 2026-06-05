package com.finsight.service;

import com.finsight.messaging.KafkaTopics;
import com.finsight.messaging.OutboxService;
import com.finsight.model.*;
import com.finsight.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final OutboxService outboxService;
    private final KafkaTopics kafkaTopics;
    
    public NotificationService(NotificationRepository notificationRepository,
                              NotificationPreferenceRepository preferenceRepository,
                              UserRepository userRepository,
                              TransactionRepository transactionRepository,
                              OutboxService outboxService,
                              KafkaTopics kafkaTopics) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.outboxService = outboxService;
        this.kafkaTopics = kafkaTopics;
    }
    
    /**
     * Initialize notification preferences for a user
     */
    public NotificationPreference initializePreferences(@NonNull Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<NotificationPreference> existing = preferenceRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        NotificationPreference pref = new NotificationPreference(user);
        NotificationPreference saved = preferenceRepository.save(pref);
        logger.info("Initialized notification preferences for user {}", userId);
        return saved;
    }

    @Transactional
    public NotificationPreference getPreferences(@NonNull Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> initializePreferences(userId));
    }

    public NotificationPreference updatePreferences(@NonNull Long userId,
                                                   boolean budgetAlertsEnabled,
                                                   int budgetAlertThreshold,
                                                   boolean alertEmail,
                                                   boolean alertInApp,
                                                   NotificationPreference.AlertFrequency alertFrequency) {
        NotificationPreference preference = getPreferences(userId);
        preference.setBudgetAlertsEnabled(budgetAlertsEnabled);
        preference.setBudgetAlertThreshold(budgetAlertThreshold);
        preference.setAlertEmail(alertEmail);
        preference.setAlertInApp(alertInApp);
        preference.setAlertFrequency(alertFrequency);
        return preferenceRepository.save(preference);
    }
    
    /**
     * Create a notification
     */
    public Notification createNotification(@NonNull Long userId, Notification.NotificationType type,
                                          String title, String message, String metadata) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = new Notification(user, type, title, message);
        notification.setMetadata(metadata);
        
        Notification saved = notificationRepository.save(notification);
        logger.info("Created {} notification for user {}", type, userId);
        publishNotificationEvent(saved);
        return saved;
    }

    private void publishNotificationEvent(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getId());
        payload.put("userId", notification.getUser().getId());
        payload.put("type", notification.getType().name());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("metadata", notification.getMetadata());
        payload.put("createdAt", notification.getCreatedAt());
        payload.put("read", notification.getRead());

        String eventKey = String.valueOf(notification.getUser().getId());

        outboxService.enqueue(
                kafkaTopics.getNotifications(),
                "notification.created",
                eventKey,
                notification.getUser().getId(),
                payload
        );

        outboxService.enqueue(
                kafkaTopics.getWebhooks(),
                "notification.created",
                eventKey,
                notification.getUser().getId(),
                payload
        );
    }
    
    /**
     * Check and create budget alert if spending exceeds threshold
     */
    public void checkAndCreateBudgetAlert(@NonNull Long userId, Budget budget) {
        Optional<NotificationPreference> pref = preferenceRepository.findByUserId(userId);
        if (pref.isEmpty() || !pref.get().getBudgetAlertsEnabled()) {
            return;
        }
        
        // Calculate spending for this month/category
        int year = budget.getYear();
        int month = budget.getMonth();
        java.time.LocalDate startOfMonth = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        
        BigDecimal spending = transactionRepository.sumExpenseByUserAndCategoryAndDateRange(
            userId,
            budget.getCategory().getId(),
            startOfMonth,
            endOfMonth
        );
        
        if (spending == null) spending = BigDecimal.ZERO;
        
        BigDecimal limit = budget.getMonthlyLimit();
        int percentageUsed = limit.compareTo(BigDecimal.ZERO) > 0 
            ? spending.divide(limit, 0, java.math.RoundingMode.HALF_UP).intValue() * 100
            : 0;
        
        int threshold = pref.get().getBudgetAlertThreshold();
        if (percentageUsed >= threshold && percentageUsed < 100) {
            // Warning alert
            createNotification(
                userId,
                Notification.NotificationType.BUDGET_ALERT,
                "Budget Alert: " + budget.getCategory().getName(),
                "You've spent " + percentageUsed + "% of your budget for " + budget.getCategory().getName(),
                null
            );
        } else if (percentageUsed >= 100) {
            // Critical alert
            createNotification(
                userId,
                Notification.NotificationType.BUDGET_ALERT,
                "Budget Exceeded: " + budget.getCategory().getName(),
                "You've exceeded your budget for " + budget.getCategory().getName() + " by " + (percentageUsed - 100) + "%",
                null
            );
        }
    }
    
    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(@NonNull Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable);
    }
    
    /**
     * Get all notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(@NonNull Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Mark notification as read
     */
    public Notification markAsRead(@NonNull Long userId, @NonNull Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
    
    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(@NonNull Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
}
