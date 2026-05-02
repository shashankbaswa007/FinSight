package com.finsight.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Deprecation Redirect Controller - Handles backward compatibility for /api paths.
 * 
 * This controller redirects all requests from the deprecated /api/* paths to /api/v1/* paths.
 * Note: Does NOT handle /api/v1/* paths - those are routed directly to versioned endpoints.
 * 
 * Deprecation Timeline:
 * - Release Date: 2026-05-02
 * - Sunset Date: 2026-08-02 (3-month deprecation period)
 * - After Sunset: /api/* endpoints will return 410 Gone
 * 
 * All redirects use HTTP 301 (Moved Permanently) with Deprecation headers.
 */
@RestController
public class DeprecationRedirectController {

    /**
     * Catch-all handler for deprecated /api paths (excluding /api/v1).
     * Redirects to /api/v1 equivalent paths.
     * 
     * Returns HTTP 301 Moved Permanently with appropriate deprecation headers.
     * Clients should update to /api/v1 paths before 2026-08-02.
     * 
     * This pattern specifically excludes /api/v1/* paths to avoid interfering
     * with the actual versioned API endpoints.
     */
    @RequestMapping(value = {
            "/api/auth/**",
            "/api/transactions/**", 
            "/api/budgets/**",
            "/api/categories/**",
            "/api/profile/**",
            "/api/recurring-transactions/**",
            "/api/analytics/**",
            "/api/export/**",
            "/api/gdpr/**"
    })
    public RedirectView redirectToV1(HttpServletRequest request) {
        // Extract the original path and redirect to /api/v1 equivalent
        String originalPath = request.getRequestURI();
        String redirectPath = originalPath.replace("/api/", "/api/v1/");

        // Return permanent redirect (301) - client should cache and update
        RedirectView view = new RedirectView(redirectPath);
        view.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return view;
    }
}
