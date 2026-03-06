package com.finsight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.dto.AnomalyResponse;
import com.finsight.dto.CategoryTrendResponse;
import com.finsight.dto.DailySpendingResponse;
import com.finsight.dto.ExpenseDistributionResponse;
import com.finsight.dto.MonthOverMonthResponse;
import com.finsight.dto.MonthlySummaryResponse;
import com.finsight.dto.SpendingTrendResponse;
import com.finsight.dto.TopCategoryResponse;
import com.finsight.dto.TopDescriptionResponse;
import com.finsight.service.AnalyticsService;
import com.finsight.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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

    @GetMapping("/month-over-month")
    @Operation(summary = "Month over month comparison", description = "Compare income and expenses with the previous month")
    public ResponseEntity<MonthOverMonthResponse> getMonthOverMonth(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getMonthOverMonth(userId, month, year));
    }

    @GetMapping("/daily-spending")
    @Operation(summary = "Daily spending", description = "Daily expense totals for a given month")
    public ResponseEntity<List<DailySpendingResponse>> getDailySpending(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getDailySpending(userId, month, year));
    }

    @GetMapping("/category-trends")
    @Operation(summary = "Category trends", description = "Spending by category across recent months")
    public ResponseEntity<List<CategoryTrendResponse>> getCategoryTrends(
            @RequestParam(defaultValue = "6") int months) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getCategoryTrends(userId, months));
    }

    @GetMapping("/expense-distribution")
    @Operation(summary = "Expense distribution", description = "Transaction amount distribution in buckets")
    public ResponseEntity<List<ExpenseDistributionResponse>> getExpenseDistribution(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getExpenseDistribution(userId, month, year));
    }

    @GetMapping("/top-descriptions")
    @Operation(summary = "Top descriptions", description = "Most frequent expense descriptions/merchants")
    public ResponseEntity<List<TopDescriptionResponse>> getTopDescriptions(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getTopDescriptions(userId, month, year, Math.min(limit, 50)));
    }
}
