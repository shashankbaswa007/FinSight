package com.finsight.controller;

import com.finsight.dto.AnomalyResponse;
import com.finsight.dto.MonthlySummaryResponse;
import com.finsight.dto.SpendingTrendResponse;
import com.finsight.dto.TopCategoryResponse;
import com.finsight.service.AnalyticsService;
import com.finsight.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for financial analytics.
 * Provides monthly summaries, top spending categories, trends, and anomaly detection.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Financial insights and spending analysis")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtil securityUtil;

    public AnalyticsController(AnalyticsService analyticsService, SecurityUtil securityUtil) {
        this.analyticsService = analyticsService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Monthly summary", description = "Total income, expenses, savings, and ratio for a month")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getMonthlySummary(userId, month, year));
    }

    @GetMapping("/top-categories")
    @Operation(summary = "Top spending categories", description = "Ranked list of categories by expense amount")
    public ResponseEntity<List<TopCategoryResponse>> getTopCategories(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getTopCategories(userId, month, year));
    }

    @GetMapping("/spending-trends")
    @Operation(summary = "Spending trends", description = "Monthly income and expense totals over recent months")
    public ResponseEntity<List<SpendingTrendResponse>> getSpendingTrends(
            @RequestParam(defaultValue = "6") int months) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getSpendingTrends(userId, months));
    }

    @GetMapping("/anomaly-detection")
    @Operation(summary = "Anomaly detection", description = "Detect abnormal spending using z-score analysis")
    public ResponseEntity<List<AnomalyResponse>> detectAnomalies() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.detectAnomalies(userId));
    }
}
