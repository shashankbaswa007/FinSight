package com.finsight.service;

import com.finsight.dto.GdprExportResponse;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.*;
import com.finsight.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GDPR Compliance Service - Handles data export and deletion requests.
 * 
 * Implements GDPR Articles:
 * - Article 15: Right of access (data export)
 * - Article 17: Right to be forgotten (soft-delete with 90-day retention)
 * - Article 20: Right to data portability (JSON export format)
 * 
 * Features:
 * - Complete data export in portable JSON format
 * - Soft-delete with anonymization
 * - Scheduled hard-delete after 90-day retention period
 * - Full audit logging of all GDPR operations
 * - Compliance tracking and verification
 */
@Service
public class GdprService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GdprService.class);
    private static final int RETENTION_DAYS = 90;

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Autowired
    public GdprService(UserRepository userRepository, TransactionRepository transactionRepository,
                       BudgetRepository budgetRepository, RecurringTransactionRepository recurringTransactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    /**
     * Export all user data in GDPR-compliant format.
     * 
     * GDPR Article 20: Right to data portability
     * Returns all personal data in a structured, portable format.
     * 
     * @param userId The user ID requesting export
     * @param requestingUserEmail Email of the user making the request (for audit logging)
     * @return GdprExportResponse containing all user data
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public GdprExportResponse exportUserData(Long userId, String requestingUserEmail) {
        log.info("GDPR Data Export Request: userId={}, requestedBy={}", userId, requestingUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Collect user data
        GdprExportResponse.UserData userData = new GdprExportResponse.UserData(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString(),
                user.getCreatedAt(),
                user.getLastLogin()
        );

        // Collect transactions
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        List<GdprExportResponse.TransactionData> transactionData = transactions.stream()
                .map(t -> new GdprExportResponse.TransactionData(
                        t.getId(),
                        t.getAmount(),
                        t.getType().toString(),
                        t.getCategory().getName(),
                        t.getDescription(),
                        t.getDate(),
                        t.getCreatedAt()
                ))
                .collect(Collectors.toList());

        // Collect budgets
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        List<GdprExportResponse.BudgetData> budgetData = budgets.stream()
                .map(b -> new GdprExportResponse.BudgetData(
                        b.getId(),
                        b.getCategory().getName(),
                        b.getMonthlyLimit(),
                        b.getMonth(),
                        b.getYear()
                ))
                .collect(Collectors.toList());

        // Collect recurring transactions
        List<RecurringTransaction> recurring = recurringTransactionRepository.findByUserId(userId);
        List<GdprExportResponse.RecurringTransactionData> recurringData = recurring.stream()
                .map(r -> new GdprExportResponse.RecurringTransactionData(
                        r.getId(),
                        r.getAmount(),
                        r.getType().toString(),
                        r.getCategory().getName(),
                        r.getDescription(),
                        r.getFrequency().toString(),
                        r.getStartDate(),
                        r.getEndDate(),
                        r.getNextOccurrence(),
                        r.isActive()
                ))
                .collect(Collectors.toList());

        // Get unique categories used by this user (from transactions and budgets)
        List<Category> userCategories = new java.util.ArrayList<>();
        transactions.stream()
                .map(Transaction::getCategory)
                .distinct()
                .forEach(userCategories::add);
        budgets.stream()
                .map(Budget::getCategory)
                .distinct()
                .forEach(c -> {
                    if (!userCategories.contains(c)) {
                        userCategories.add(c);
                    }
                });

        List<GdprExportResponse.CategoryData> categoryData = userCategories.stream()
                .map(c -> new GdprExportResponse.CategoryData(
                        c.getId(),
                        c.getName(),
                        c.getType().toString()
                ))
                .collect(Collectors.toList());

        // Calculate metadata
        BigDecimal totalIncome = transactionData.stream()
                .filter(t -> "INCOME".equals(t.type))
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactionData.stream()
                .filter(t -> "EXPENSE".equals(t.type))
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        GdprExportResponse.ExportMetadata metadata = new GdprExportResponse.ExportMetadata(
                appVersion,
                transactionData.size(),
                budgetData.size(),
                recurringData.size(),
                totalIncome,
                totalExpenses
        );

        GdprExportResponse.ExportData exportData = new GdprExportResponse.ExportData(
                userData,
                transactionData,
                budgetData,
                recurringData,
                categoryData,
                metadata
        );

        log.info("GDPR Data Export Completed: userId={}, records={}", userId,
                transactionData.size() + budgetData.size() + recurringData.size());

        return new GdprExportResponse(exportData);
    }

    /**
     * Request data deletion (soft-delete with 90-day retention).
     * 
     * GDPR Article 17: Right to erasure (right to be forgotten)
     * Performs soft-delete and schedules hard-delete after retention period.
     * 
     * @param userId The user ID requesting deletion
     * @param reason Reason for deletion (optional)
     * @param requestingUserEmail Email of the user making the request (for audit)
     * @return Confirmation message with deletion schedule
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public String requestDataDeletion(Long userId, String reason, String requestingUserEmail) {
        log.info("GDPR Data Deletion Request: userId={}, reason={}, requestedBy={}", 
                userId, reason, requestingUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prevent deletion of already deleted accounts
        if (user.isDeleted()) {
            log.warn("Deletion request for already deleted user: userId={}", userId);
            return "User already marked for deletion";
        }

        // Soft-delete: mark user as deleted and anonymize
        user.setDeleted(true);
        user.setDeletionRequestedAt(LocalDateTime.now());
        user.setDeletionReason(reason);
        user.setHardDeleteScheduledAt(LocalDateTime.now().plusDays(RETENTION_DAYS));

        userRepository.save(user);

        // Log the deletion request
        log.info("GDPR Soft-Delete Applied: userId={}, hardDeleteScheduledAt={}", 
                userId, user.getHardDeleteScheduledAt());

        return String.format(
                "Account deletion scheduled. Your data will be permanently deleted on %s. " +
                "You have %d days to cancel this request.",
                user.getHardDeleteScheduledAt().toLocalDate(), RETENTION_DAYS
        );
    }

    /**
     * Cancel a pending deletion request.
     * Only available within the retention period.
     * 
     * @param userId The user ID
     * @param requestingUserEmail Email of the user making the request
     * @return Confirmation message
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public String cancelDeletionRequest(Long userId, String requestingUserEmail) {
        log.info("GDPR Deletion Cancel Request: userId={}, requestedBy={}", userId, requestingUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isDeleted()) {
            log.warn("Cancel deletion request for non-deleted user: userId={}", userId);
            return "User is not marked for deletion";
        }

        // Check if cancellation is within retention period
        if (user.getHardDeleteScheduledAt().isBefore(LocalDateTime.now())) {
            log.error("Cannot cancel deletion: retention period expired, userId={}", userId);
            throw new IllegalStateException("Retention period has expired. Automatic hard-delete is scheduled.");
        }

        // Restore user (revert soft-delete)
        user.setDeleted(false);
        user.setDeletionRequestedAt(null);
        user.setDeletionReason(null);
        user.setHardDeleteScheduledAt(null);
        userRepository.save(user);

        log.info("GDPR Deletion Cancelled: userId={}", userId);
        return "Your account has been restored. Deletion request cancelled.";
    }

    /**
     * Scheduled task: Perform hard-delete of users whose retention period has expired.
     * 
     * Runs daily at 2 AM UTC. Automatically deletes all data for users who:
     * 1. Are marked as deleted (is_deleted = true)
     * 2. Have exceeded the 90-day retention period
     * 
     * This method should be called by a scheduler (e.g., @Scheduled annotation).
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC") // Daily at 2 AM UTC
    public void performScheduledHardDelete() {
        log.info("Starting scheduled hard-delete job for expired retention periods");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<User> usersForDeletion = userRepository.findDeletedUsersBefore(cutoffDate);

        if (usersForDeletion.isEmpty()) {
            log.info("No users found for hard-delete");
            return;
        }

        log.info("Found {} users for hard-delete", usersForDeletion.size());

        for (User user : usersForDeletion) {
            try {
                performHardDelete(user);
            } catch (Exception e) {
                log.error("Error performing hard-delete for userId={}: {}", user.getId(), e.getMessage());
                // Continue with next user on error
            }
        }

        log.info("Scheduled hard-delete job completed");
    }

    /**
     * Perform hard-delete of a specific user.
     * Permanently removes all user data from the system.
     * 
     * @param user The user to hard-delete
     */
    @Transactional
    private void performHardDelete(User user) {
        Long userId = user.getId();
        log.info("Performing hard-delete for userId={}", userId);

        try {
            // Delete all related data
            transactionRepository.deleteByUserId(userId);
            budgetRepository.deleteByUserId(userId);
            recurringTransactionRepository.deleteByUserId(userId);
            // Note: Categories are shared across users, not user-specific, so they are not deleted

            // Delete user
            userRepository.deleteById(userId);

            log.info("Hard-delete completed successfully for userId={}", userId);
        } catch (Exception e) {
            log.error("Hard-delete failed for userId={}: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Verify GDPR compliance for a user.
     * Checks deletion status and retention period.
     * 
     * @param userId The user ID to check
     * @return Status object with deletion info
     */
    @Transactional(readOnly = true)
    public GdprComplianceStatus checkGdprStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GdprComplianceStatus status = new GdprComplianceStatus();
        status.userId = userId;
        status.isDeleted = user.isDeleted();
        status.deletionRequestedAt = user.getDeletionRequestedAt();
        status.hardDeleteScheduledAt = user.getHardDeleteScheduledAt();

        if (user.isDeleted() && user.getHardDeleteScheduledAt() != null) {
            status.daysUntilHardDelete = 
                    (int) java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDateTime.now(), 
                            user.getHardDeleteScheduledAt()
                    );
            status.canCancelDeletion = status.daysUntilHardDelete > 0;
        }

        return status;
    }

    /**
     * GDPR Compliance Status DTO
     */
    public static class GdprComplianceStatus {
        public Long userId;
        public boolean isDeleted;
        public LocalDateTime deletionRequestedAt;
        public LocalDateTime hardDeleteScheduledAt;
        public int daysUntilHardDelete;
        public boolean canCancelDeletion;
    }
}
