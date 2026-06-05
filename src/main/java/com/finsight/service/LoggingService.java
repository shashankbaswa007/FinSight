package com.finsight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for centralized logging with correlation ID tracking and MDC context.
 * Provides structured logging patterns for audit and debugging.
 */
@Service
public class LoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    
    /**
     * Initialize correlation ID in MDC
     * Creates new UUID if not provided
     */
    public static String initializeCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        return correlationId;
    }
    
    /**
     * Set user ID in MDC for request tracking
     */
    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID_MDC_KEY, userId.toString());
        }
    }
    
    /**
     * Set request ID in MDC
     */
    public static void setRequestId(String requestId) {
        if (requestId != null) {
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
        }
    }
    
    /**
     * Clear MDC context
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Get correlation ID from MDC
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }
    
    /**
     * Log audit event
     */
    public void logAuditEvent(Long userId, String action, String resource, String result) {
        logger.info("AUDIT_EVENT: action={}, resource={}, userId={}, result={}", 
            action, resource, userId, result);
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String event, String ipAddress, String username, String result) {
        logger.warn("SECURITY_EVENT: event={}, username={}, ip={}, result={}", 
            event, username, ipAddress, result);
    }
    
    /**
     * Log performance metric
     */
    public void logPerformanceMetric(String endpoint, String method, long durationMs, int statusCode) {
        logger.info("PERFORMANCE_METRIC: endpoint={}, method={}, duration_ms={}, status_code={}", 
            endpoint, method, durationMs, statusCode);
    }
    
    /**
     * Log error with context
     */
    public void logError(String message, Throwable ex, String context) {
        logger.error("ERROR: message={}, context={}, exception={}", 
            message, context, ex.getClass().getSimpleName(), ex);
    }
}
