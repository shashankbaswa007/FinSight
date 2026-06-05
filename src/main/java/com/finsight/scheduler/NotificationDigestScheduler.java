package com.finsight.scheduler;

import com.finsight.model.Notification;
import com.finsight.model.NotificationPreference;
import com.finsight.repository.NotificationPreferenceRepository;
import com.finsight.repository.NotificationRepository;
import com.finsight.service.NotificationDigestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "finsight.notifications.digest", name = "enabled", havingValue = "true")
public class NotificationDigestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDigestScheduler.class);

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDigestService digestService;

    @Value("${finsight.notifications.digest.max-items:100}")
    private int maxItems;

    @Value("${finsight.notifications.digest.weekly-day:MONDAY}")
    private String weeklyDay;

    public NotificationDigestScheduler(NotificationPreferenceRepository preferenceRepository,
                                       NotificationRepository notificationRepository,
                                       NotificationDigestService digestService) {
        this.preferenceRepository = preferenceRepository;
        this.notificationRepository = notificationRepository;
        this.digestService = digestService;
    }

    @Scheduled(cron = "${finsight.notifications.digest.cron:0 0 8 * * *}")
    @Transactional
    public void enqueueDigests() {
        DayOfWeek weeklyRunDay = parseWeeklyDay();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<NotificationPreference> preferences = preferenceRepository
                .findByAlertEmailTrueAndAlertFrequencyIn(
                        List.of(NotificationPreference.AlertFrequency.DAILY,
                                NotificationPreference.AlertFrequency.WEEKLY)
                );

        for (NotificationPreference preference : preferences) {
            if (preference.getUser() == null) {
                continue;
            }

            NotificationPreference.AlertFrequency frequency = preference.getAlertFrequency();
            if (frequency == NotificationPreference.AlertFrequency.WEEKLY
                    && today.getDayOfWeek() != weeklyRunDay) {
                continue;
            }

            LocalDateTime start = frequency == NotificationPreference.AlertFrequency.DAILY
                    ? now.minusDays(1)
                    : now.minusDays(7);

            List<Notification> notifications = notificationRepository
                    .findByUserIdAndReadFalseAndCreatedAtBetween(
                            preference.getUser().getId(),
                            start,
                            now
                    );

            if (notifications.isEmpty()) {
                continue;
            }

            List<Long> notificationIds = new ArrayList<>();
            for (Notification notification : notifications) {
                notificationIds.add(notification.getId());
            }

            for (int i = 0; i < notificationIds.size(); i += maxItems) {
                int end = Math.min(notificationIds.size(), i + maxItems);
                List<Long> batch = notificationIds.subList(i, end);
                digestService.enqueueDigest(preference.getUser().getId(), frequency, start, now, batch);
            }
        }

        logger.info("Notification digest enqueue cycle complete");
    }

    private DayOfWeek parseWeeklyDay() {
        try {
            return DayOfWeek.valueOf(weeklyDay.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            return DayOfWeek.MONDAY;
        }
    }
}
