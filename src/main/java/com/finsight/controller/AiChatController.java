package com.finsight.controller;

import com.finsight.service.AiService;
import com.finsight.service.AnalyticsService;
import com.finsight.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Insights", description = "AI-powered financial advisor and utilities")
public class AiChatController {

    private final AiService aiService;
    private final AnalyticsService analyticsService;
    private final SecurityUtil securityUtil;

    public AiChatController(AiService aiService, AnalyticsService analyticsService, SecurityUtil securityUtil) {
        this.aiService = aiService;
        this.analyticsService = analyticsService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/chat")
    @Operation(summary = "Ask the AI Financial Advisor a question")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        Long userId = securityUtil.getCurrentUserId();
        String userMessage = request.get("message");
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        // Fetch recent context for the AI
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        int currentYear = java.time.LocalDate.now().getYear();
        
        // Use the analytics service to get a summary to feed to the LLM
        String contextData = "";
        try {
            var summary = analyticsService.getMonthlySummary(userId, currentMonth, currentYear);
            contextData = String.format("Current Month (%02d/%d) Status:\nTotal Income: %s\nTotal Expenses: %s\nNet Savings: %s",
                    currentMonth, currentYear, 
                    summary.getTotalIncome(), summary.getTotalExpense(), summary.getNetSavings());
        } catch (Exception e) {
            contextData = "No recent data available.";
        }

        String aiResponse = aiService.getFinancialAdvice(userId, userMessage, contextData);
        return ResponseEntity.ok(Map.of("reply", aiResponse));
    }
}
