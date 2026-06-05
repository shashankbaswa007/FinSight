package com.finsight.service;

import com.finsight.model.Notification;
import com.finsight.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailService.class);

    @Value("${finsight.notifications.email.enabled:false}")
    private boolean emailEnabled;

    public boolean sendDigest(User user, List<Notification> notifications, String subject) {
        if (!emailEnabled) {
            logger.info("Email sending disabled; skipping digest email to {}", user.getEmail());
            return false;
        }

        String preview = notifications.stream()
                .limit(5)
                .map(Notification::getTitle)
                .collect(Collectors.joining(", "));
        logger.info("Sending digest email to {} ({} notifications): {}", user.getEmail(), notifications.size(), preview);
        return true;
    }
}
