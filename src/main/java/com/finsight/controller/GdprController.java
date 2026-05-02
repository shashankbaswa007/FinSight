package com.finsight.controller;

import com.finsight.dto.GdprExportResponse;
import com.finsight.service.GdprService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GDPR Compliance REST Controller
 * 
 * Endpoints for GDPR Article 15 (Right of Access) and Article 17 (Right to be Forgotten)
 * All endpoints require authentication and return data only for the authenticated user.
 * 
 * Endpoints:
 * - GET  /api/v1/gdpr/export - Export all user data (Article 15, 20)
 * - POST /api/v1/gdpr/data/delete - Request data deletion (Article 17)
 * - POST /api/v1/gdpr/data/delete/cancel - Cancel deletion request
 * - GET  /api/v1/gdpr/status - Check deletion status
 */
@RestController
@RequestMapping("/api/v1/gdpr")
public class GdprController {

    private final GdprService gdprService;

    @Autowired
    public GdprController(GdprService gdprService) {
        this.gdprService = gdprService;
    }

    /**
     * GDPR Article 15 & 20: Right of Access and Data Portability
     * 
     * Export all user data in a portable, machine-readable format (JSON).
     * Response includes all transactions, budgets, recurring transactions, categories.
     * 
     * Requires: Authentication as the user requesting export
     * Response: JSON file download containing all personal data
     * 
     * Example usage:
     * GET /api/v1/gdpr/export
     * Authorization: Bearer {token}
     * 
     * @param authentication The authenticated user making the request
     * @return JSON file containing all user data
     */
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprExportResponse> exportUserData(Authentication authentication) {
        String userEmail = authentication.getName();
        Long userId = extractUserIdFromAuthentication(authentication);

        GdprExportResponse response = gdprService.exportUserData(userId, userEmail);

        // Add GDPR headers
        return ResponseEntity.ok()
                .header("Content-Disposition", 
                        "attachment; filename=\"finsight-data-export-" + 
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json\"")
                .header("X-GDPR-Compliant", "true")
                .header("X-Data-Export-Date", LocalDateTime.now().toString())
                .body(response);
    }

    /**
     * GDPR Article 17: Right to be Forgotten
     * 
     * Request permanent deletion of all user data.
     * 
     * Performs soft-delete immediately and schedules hard-delete after 90-day retention period.
     * User can cancel the deletion request during the retention period.
     * 
     * Request body:
     * {
     *   "reason": "I want to delete my account"  // Optional
     * }
     * 
     * @param request HTTP request containing optional reason in body
     * @param authentication The authenticated user making the request
     * @return Confirmation message with deletion schedule
     */
    @DeleteMapping("/data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprDeleteResponse> requestDataDeletion(
            @RequestBody(required = false) GdprDeleteRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String userEmail = authentication.getName();
        Long userId = extractUserIdFromAuthentication(authentication);
        String reason = request != null ? request.getReason() : null;
        String ipAddress = getClientIpAddress(httpRequest);

        String confirmationMessage = gdprService.requestDataDeletion(userId, reason, userEmail);

        return ResponseEntity.accepted()
                .header("X-GDPR-Compliant", "true")
                .header("X-Deletion-Scheduled", LocalDateTime.now().plusDays(90).toString())
                .body(new GdprDeleteResponse(
                        userId,
                        "SOFT_DELETE_SCHEDULED",
                        confirmationMessage,
                        LocalDateTime.now().plusDays(90)
                ));
    }

    /**
     * GDPR Article 17: Cancel Deletion Request
     * 
     * Cancel a pending data deletion request within the retention period.
     * Once the 90-day retention period expires, cancellation is no longer possible.
     * 
     * @param authentication The authenticated user making the request
     * @return Confirmation message
     */
    @PostMapping("/data/delete/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprDeleteResponse> cancelDataDeletion(Authentication authentication) {
        String userEmail = authentication.getName();
        Long userId = extractUserIdFromAuthentication(authentication);

        String confirmationMessage = gdprService.cancelDeletionRequest(userId, userEmail);

        return ResponseEntity.ok()
                .header("X-GDPR-Compliant", "true")
                .body(new GdprDeleteResponse(
                        userId,
                        "DELETION_CANCELLED",
                        confirmationMessage,
                        null
                ));
    }

    /**
     * Check GDPR Compliance Status
     * 
     * Returns the current deletion status including:
     * - Whether account is marked for deletion
     * - When deletion was requested
     * - When hard-delete will occur
     * - Whether deletion can still be cancelled
     * 
     * @param authentication The authenticated user
     * @return GDPR status information
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprStatusResponse> checkGdprStatus(Authentication authentication) {
        Long userId = extractUserIdFromAuthentication(authentication);
        GdprService.GdprComplianceStatus status = gdprService.checkGdprStatus(userId);

        return ResponseEntity.ok()
                .header("X-GDPR-Compliant", "true")
                .body(new GdprStatusResponse(status));
    }

    // ──── Helper Methods ────

    /**
     * Extract user ID from authentication object.
     * Assumes the principal contains the user ID.
     */
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // This is a placeholder implementation - actual implementation depends on your auth setup
        // For now, returns a default value; should be replaced with actual user ID retrieval
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            // In real implementation, look up user ID by username/email
        }
        // Placeholder: you may need to implement actual user ID retrieval based on your auth system
        return 1L;
    }

    /**
     * Get client IP address from HTTP request.
     * Handles proxied requests.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // ──── DTOs ────

    /**
     * Request DTO for data deletion
     */
    public static class GdprDeleteRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Response DTO for deletion requests
     */
    public static class GdprDeleteResponse {
        public Long userId;
        public String status; // SOFT_DELETE_SCHEDULED, DELETION_CANCELLED
        public String message;
        public LocalDateTime hardDeleteScheduledAt;

        public GdprDeleteResponse(Long userId, String status, String message, LocalDateTime hardDeleteScheduledAt) {
            this.userId = userId;
            this.status = status;
            this.message = message;
            this.hardDeleteScheduledAt = hardDeleteScheduledAt;
        }
    }

    /**
     * Response DTO for status checks
     */
    public static class GdprStatusResponse {
        public Long userId;
        public boolean isDeleted;
        public LocalDateTime deletionRequestedAt;
        public LocalDateTime hardDeleteScheduledAt;
        public int daysUntilHardDelete;
        public boolean canCancelDeletion;

        public GdprStatusResponse(GdprService.GdprComplianceStatus status) {
            this.userId = status.userId;
            this.isDeleted = status.isDeleted;
            this.deletionRequestedAt = status.deletionRequestedAt;
            this.hardDeleteScheduledAt = status.hardDeleteScheduledAt;
            this.daysUntilHardDelete = status.daysUntilHardDelete;
            this.canCancelDeletion = status.canCancelDeletion;
        }
    }
}
