package com.finsight.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Base Controller for API versioning and deprecation management.
 * 
 * Version Strategy:
 * - Current API Version: /api/v1 (since 2026-05-02)
 * - Deprecated: /api (sunset: 2026-08-02, 3-month deprecation period)
 * - Next Version: /api/v2 (planned for Q3 2026)
 * 
 * Headers added to all responses:
 * - API-Version: v1
 * - Deprecation: false (for /api/v1) or true (for deprecated paths)
 * - Sunset: 2026-08-02 (for deprecated paths)
 */
@RestController
@RequestMapping("/api/v1")
public class BaseController {

    // API Version Constants
    public static final String API_VERSION = "v1";
    public static final String CURRENT_API_PATH = "/api/v1";
    public static final String DEPRECATED_API_PATH = "/api";
    public static final String SUNSET_DATE = "Fri, 02 Aug 2026 00:00:00 GMT";
    public static final String DEPRECATION_REASON = "API v1 will be sunset on 2026-08-02. Please migrate to /api/v2";

    /**
     * Health check endpoint for API availability.
     * 
     * @return API version and status information
     */
    @GetMapping("/health")
    public ResponseEntity<VersionInfo> health() {
        VersionInfo info = new VersionInfo(
                API_VERSION,
                "FinSight API",
                "OK",
                LocalDate.now().toString()
        );

        return ResponseEntity.ok()
                .header("API-Version", API_VERSION)
                .body(info);
    }

    /**
     * API version information endpoint.
     * Returns current API version, deprecation status, and migration information.
     * 
     * @return Version details
     */
    @GetMapping("/version")
    public ResponseEntity<VersionDetails> versionInfo() {
        VersionDetails details = new VersionDetails(
                API_VERSION,
                "2026-05-02",
                "2026-08-02",
                "/api/v2",
                false
        );

        return ResponseEntity.ok()
                .header("API-Version", API_VERSION)
                .body(details);
    }

    /**
     * Add standard API headers to response.
     * Should be called by all endpoints to maintain consistency.
     */
    protected ResponseEntity<?> withApiHeaders(Object body) {
        return ResponseEntity.ok()
                .header("API-Version", API_VERSION)
                .body(body);
    }

    /**
     * Version Info DTO
     */
    public static class VersionInfo {
        public String version;
        public String name;
        public String status;
        public String timestamp;

        public VersionInfo(String version, String name, String status, String timestamp) {
            this.version = version;
            this.name = name;
            this.status = status;
            this.timestamp = timestamp;
        }
    }

    /**
     * Version Details DTO with deprecation information
     */
    public static class VersionDetails {
        public String currentVersion;
        public String releaseDate;
        public String sunsetDate;
        public String nextVersion;
        public boolean isDeprecated;

        public VersionDetails(String currentVersion, String releaseDate, String sunsetDate,
                            String nextVersion, boolean isDeprecated) {
            this.currentVersion = currentVersion;
            this.releaseDate = releaseDate;
            this.sunsetDate = sunsetDate;
            this.nextVersion = nextVersion;
            this.isDeprecated = isDeprecated;
        }
    }
}
