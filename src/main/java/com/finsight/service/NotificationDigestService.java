package com.finsight.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.finsight.messaging.KafkaEvent;
import com.finsight.messaging.KafkaTopics;
import com.finsight.messaging.OutboxService;
import com.finsight.model.Notification;
import com.finsight.model.NotificationDelivery;
import com.finsight.model.NotificationPreference;
import com.finsight.model.User;
import com.finsight.repository.NotificationDeliveryRepository;
import com.finsight.repository.NotificationRepository;
import com.finsight.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class NotificationDigestService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDigestService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
    private final KafkaTopics kafkaTopics;
    private final NotificationEmailService emailService;

    public NotificationDigestService(NotificationRepository notificationRepository,
                                     NotificationDeliveryRepository notificationDeliveryRepository,
                                     UserRepository userRepository,
                                     OutboxService outboxService,
                                     KafkaTopics kafkaTopics,
                                     NotificationEmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.userRepository = userRepository;
        this.outboxService = outboxService;
        this.kafkaTopics = kafkaTopics;
        this.emailService = emailService;
    }

    public void enqueueDigest(Long userId,
                              NotificationPreference.AlertFrequency frequency,
                              LocalDateTime start,
                              LocalDateTime end,
                              List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("frequency", frequency.name());
        payload.put("start", start);
        payload.put("end", end);
        payload.put("notificationIds", notificationIds);
        payload.put("count", notificationIds.size());

        outboxService.enqueue(
                kafkaTopics.getDigests(),
                "notification.digest.ready",
                String.valueOf(userId),
                userId,
                payload
        );
    }

    public void handleDigestEvent(KafkaEvent event) {
        if (event == null || event.getPayload() == null) {
            return;
        }

        JsonNode payload = event.getPayload();
        Long userId = event.getUserId();
        if (userId == null && payload.has("userId")) {
            userId = payload.get("userId").asLong();
        }
        if (userId == null) {
            return;
        }
        final Long resolvedUserId = userId;

        List<Long> notificationIds = parseNotificationIds(payload.get("notificationIds"));
        if (notificationIds.isEmpty()) {
            return;
        }

        Set<Long> deliveredIds = new HashSet<>(
                notificationDeliveryRepository.findDeliveredNotificationIds(
                        notificationIds,
                        NotificationDelivery.Channel.EMAIL
                )
        );
        List<Long> pendingIds = new ArrayList<>();
        for (Long id : notificationIds) {
            if (!deliveredIds.contains(id)) {
                pendingIds.add(id);
            }
        }
        if (pendingIds.isEmpty()) {
            return;
        }

        User user = userRepository.findById(resolvedUserId).orElse(null);
        if (user == null) {
            return;
        }

        List<Notification> notifications = notificationRepository.findAllById(pendingIds);
        notifications.removeIf(n -> n.getUser() == null || !resolvedUserId.equals(n.getUser().getId()));
        if (notifications.isEmpty()) {
            return;
        }

        boolean sent = emailService.sendDigest(user, notifications, "Your FinSight notification digest");
        LocalDateTime now = LocalDateTime.now();
        List<NotificationDelivery> deliveries = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationDelivery delivery = new NotificationDelivery(notification, NotificationDelivery.Channel.EMAIL);
            delivery.setAttemptCount(1);
            if (sent) {
                delivery.setStatus(NotificationDelivery.Status.SENT);
                delivery.setSentAt(now);
            } else {
                delivery.setStatus(NotificationDelivery.Status.FAILED);
                delivery.setLastError("Email delivery disabled or failed");
            }
            deliveries.add(delivery);
        }

        notificationDeliveryRepository.saveAll(deliveries);
        logger.info("Processed digest for user {} with {} notifications", resolvedUserId, deliveries.size());
    }

    private List<Long> parseNotificationIds(JsonNode node) {
        List<Long> ids = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return ids;
        }
        for (JsonNode entry : node) {
            if (entry.canConvertToLong()) {
                ids.add(entry.asLong());
            }
        }
        return ids;
    }
}
