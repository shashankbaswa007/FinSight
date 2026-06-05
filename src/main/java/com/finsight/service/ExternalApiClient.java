package com.finsight.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for making resilient external API calls with circuit breaker pattern.
 * Handles retry logic, timeout management, and fallback strategies.
 */
@Service
public class ExternalApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiClient.class);
    private static final int TIMEOUT_SECONDS = 10;
    
    private final HttpClient httpClient;
    private final Map<String, String> responseCache = new HashMap<>();
    
    public ExternalApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
    }
    
    /**
     * GET request with circuit breaker and retry logic
     * Falls back to cached response if available
     */
    @CircuitBreaker(name = "externalApi", fallbackMethod = "fallback")
    @Retry(name = "externalApi")
    public String getRequest(String url, String cacheKey) throws Exception {
        logger.debug("Calling external API: {}", url);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .header("Accept", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            responseCache.put(cacheKey, response.body());
            logger.info("External API call successful for: {}", url);
            return response.body();
        } else {
            throw new RuntimeException("API returned status: " + response.statusCode());
        }
    }
    
    /**
     * Fallback method when circuit breaker is open or call fails
     */
    public String fallback(String url, String cacheKey, Exception ex) {
        logger.warn("Circuit breaker triggered or API call failed for: {}. Error: {}", url, ex.getMessage());
        
        // Return cached response if available
        String cached = responseCache.get(cacheKey);
        if (cached != null) {
            logger.info("Returning cached response for: {}", cacheKey);
            return cached;
        }
        
        // Return default empty response
        logger.error("No cached response available for: {}", cacheKey);
        return "{}";
    }
    
    /**
     * Clear response cache
     */
    public void clearCache() {
        responseCache.clear();
        logger.info("Cleared external API response cache");
    }
}
