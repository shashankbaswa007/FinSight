package com.finsight.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Audit logging filter that logs every API request with method, URI, user, and response status.
 * 
 * Features:
 * - Logs request details (method, URI, status, duration)
 * - Masks sensitive headers (Authorization)
 * - Masks sensitive endpoints (/auth/login, /auth/register, /profile/password)
 * - Masks PII in log output (email addresses, phone numbers)
 * - Adds correlation ID (X-Request-ID) to every request for distributed tracing
 * 
 * Compliance: GDPR Article 5 (data minimization) - sensitive data masked in audit logs
 */
@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");
    private static final Logger piiAccess = LoggerFactory.getLogger("PII_ACCESS");
    
    private static final Set<String> SENSITIVE_ENDPOINTS = Set.of(
            "/api/auth/login", "/api/auth/register", "/api/profile/password",
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/profile/password"
    );
    
    private static final String CORRELATION_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        
        // Generate or retrieve correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Store correlation ID in MDC for logging (available to all loggers in this thread)
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        try {
            long start = System.currentTimeMillis();
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - start;

            String user = getUsername();
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            // Mask body details for sensitive endpoints
            String detail = SENSITIVE_ENDPOINTS.contains(uri) ? " [body=MASKED]" : "";

            // Log with correlation ID
            audit.info("{} {} {} status={} duration={}ms correlationId={}{}", 
                    method, uri, maskPII(user), status, duration, correlationId, detail);

            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
        } finally {
            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user=" + auth.getName();
        }
        return "anonymous";
    }

    /**
     * Masks personally identifiable information (PII) in log output.
     * 
     * Masks:
     * - Email addresses (user@example.com → user@*****.com)
     * - Phone numbers (123456789 → 123***789)
     * - Full names (kept as is, but could be masked in future)
     * 
     * @param input The input string to mask
     * @return The masked string
     */
    private String maskPII(String input) {
        if (input == null) return input;
        
        // Mask email addresses (pattern: user@domain.com → user@*****.com)
        input = input.replaceAll("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", 
                "$1@*****");
        
        // Mask phone numbers (pattern: 1234567890 → 123****890)
        input = input.replaceAll("(\\d{3})(\\d{3,4})(\\d{4})", "$1****$3");
        
        // Mask credit card numbers (pattern: 1234567890123456 → 1234****3456)
        input = input.replaceAll("(\\d{4})(\\d{8})(\\d{4})", "$1****$3");
        
        // Mask SSN (pattern: 123-45-6789 → ***-**-6789)
        input = input.replaceAll("(\\d{3})-(\\d{2})-(\\d{4})", "***-**-$3");
        
        return input;
    }
}
