package com.finsight.scheduler;

import com.finsight.dto.ReconciliationBatchResponse;
import com.finsight.model.Notification;
import com.finsight.model.ReconciliationSchedulePreference;
import com.finsight.model.User;
import com.finsight.repository.ReconciliationBatchRepository;
import com.finsight.service.ReconciliationScheduleService;
import com.finsight.service.ReconciliationService;
import com.finsight.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "finsight.reconciliation.scheduled", name = "enabled", havingValue = "true")
public class ScheduledReconciliationTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledReconciliationTask.class);

    private final ReconciliationService reconciliationService;
    private final ReconciliationScheduleService scheduleService;
    private final ReconciliationBatchRepository reconciliationBatchRepository;
    private final NotificationService notificationService;

    public ScheduledReconciliationTask(ReconciliationService reconciliationService,
                                      ReconciliationScheduleService scheduleService,
                                      ReconciliationBatchRepository reconciliationBatchRepository,
                                      NotificationService notificationService) {
        this.reconciliationService = reconciliationService;
        this.scheduleService = scheduleService;
        this.reconciliationBatchRepository = reconciliationBatchRepository;
        this.notificationService = notificationService;
    }

    /**
     * Runs daily at configured cron (default: 2 AM server local time) and performs reconciliation
     * for each user for the previous day. Disabled by default (property controlled).
     */
    @Scheduled(cron = "${finsight.reconciliation.scheduled.cron:0 0 2 * * *}")
    public void runDailyReconciliation() {
        LocalDate batchDate = LocalDate.now().minusDays(1);
        logger.info("Scheduled reconciliation started for date={}", batchDate);

        for (ReconciliationSchedulePreference preference : scheduleService.getEnabledPreferences()) {
            User user = preference.getUser();
            Long userId = Objects.requireNonNull(user.getId(), "userId");
            try {
                // Initialize batch only if not present
                boolean exists = reconciliationBatchRepository.findByUserIdAndBatchDate(userId, batchDate).isPresent();
                if (!exists) {
                    reconciliationService.initializeReconciliationBatch(userId, batchDate);
                }

                ReconciliationBatchResponse result = reconciliationService.performReconciliation(userId, batchDate);
                Integer totalTransactions = result.getTotalTransactions();
                Integer matchedTransactions = result.getMatchedTransactions();
                Integer unmatchedTransactions = result.getUnmatchedTransactions();
                int total = totalTransactions == null ? 0 : totalTransactions;
                int matched = matchedTransactions == null ? 0 : matchedTransactions;
                int unmatched = unmatchedTransactions == null ? 0 : unmatchedTransactions;
                String discrepancy = result.getDiscrepancyAmount() != null
                    ? result.getDiscrepancyAmount().toPlainString()
                    : "0";
                String message = "Scheduled reconciliation for " + batchDate + " completed. "
                    + "Matched " + matched + " of " + total
                    + " transactions; unmatched " + unmatched
                    + ". Discrepancy " + discrepancy + ".";
                String metadata = "{\"batchId\":" + result.getId()
                    + ",\"batchDate\":\"" + result.getBatchDate() + "\"}";
                notificationService.createNotification(
                    userId,
                    Notification.NotificationType.RECONCILIATION_ALERT,
                    "Reconciliation complete",
                    message,
                    metadata
                );
                logger.info("Scheduled reconciliation completed for user={} date={}", userId, batchDate);
            } catch (Exception ex) {
                logger.error("Scheduled reconciliation failed for user={} date={}", userId, batchDate, ex);
                String errorMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                String message = "Scheduled reconciliation failed for " + batchDate
                    + ". Error: " + errorMessage;
                notificationService.createNotification(
                    userId,
                    Notification.NotificationType.RECONCILIATION_ALERT,
                    "Reconciliation failed",
                    message,
                    null
                );
            }
        }
        logger.info("Scheduled reconciliation finished for date={}", batchDate);
    }
}
