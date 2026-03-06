package com.finsight.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Audit logging filter that logs every API request with method, URI, user, and response status.
 * Sensitive headers (Authorization) and sensitive endpoints (password changes) are masked.
 */
@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");
    private static final Set<String> SENSITIVE_ENDPOINTS = Set.of(
            "/api/auth/login", "/api/auth/register", "/api/profile/password"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start;
        String user = getUsername();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // Mask body details for sensitive endpoints
        String detail = SENSITIVE_ENDPOINTS.contains(uri) ? " [body=MASKED]" : "";

        audit.info("{} {} {} status={} duration={}ms{}", method, uri, user, status, duration, detail);
    }

    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user=" + auth.getName();
        }
        return "anonymous";
    }
}
