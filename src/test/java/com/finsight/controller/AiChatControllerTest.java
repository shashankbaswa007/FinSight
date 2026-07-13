package com.finsight.controller;

import com.finsight.dto.MonthlySummaryResponse;
import com.finsight.security.JwtTokenProvider;
import com.finsight.repository.IdempotencyRepository;
import com.finsight.repository.UserRepository;
import com.finsight.service.AiService;
import com.finsight.service.AnalyticsService;
import com.finsight.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AI Chat Controller.
 * Validates request validation, context assembly, and user isolation.
 */
@WebMvcTest(AiChatController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing
class AiChatControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AiService aiService;
    @MockBean private AnalyticsService analyticsService;
    @MockBean private SecurityUtil securityUtil;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private IdempotencyRepository idempotencyRepository;
    @MockBean private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
    }

    @Test
    @DisplayName("Should return AI response for valid message")
    @WithMockUser
    void shouldReturnAiResponseForValidMessage() throws Exception {
        when(aiService.getFinancialAdvice(eq(1L), eq("What are my expenses?"), anyString()))
                .thenReturn("Your total expenses this month are **₹45,000**.");

        MonthlySummaryResponse summary = new MonthlySummaryResponse();
        summary.setTotalIncome(BigDecimal.valueOf(75000));
        summary.setTotalExpense(BigDecimal.valueOf(45000));
        summary.setNetSavings(BigDecimal.valueOf(30000));
        when(analyticsService.getMonthlySummary(eq(1L), anyInt(), anyInt())).thenReturn(summary);

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("message", "What are my expenses?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value(containsString("₹45,000")));

        verify(aiService).getFinancialAdvice(eq(1L), eq("What are my expenses?"), anyString());
    }

    @Test
    @DisplayName("Should return 400 for empty message")
    @WithMockUser
    void shouldRejectEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("message", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(aiService, never()).getFinancialAdvice(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 400 when message field is missing")
    @WithMockUser
    void shouldRejectMissingMessageField() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", "test"))))
                .andExpect(status().isBadRequest());

        verify(aiService, never()).getFinancialAdvice(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should pass correct userId from security context")
    @WithMockUser
    void shouldPassCorrectUserId() throws Exception {
        when(securityUtil.getCurrentUserId()).thenReturn(42L);
        when(aiService.getFinancialAdvice(eq(42L), anyString(), anyString())).thenReturn("Response");
        when(analyticsService.getMonthlySummary(eq(42L), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("No data"));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("message", "Hello"))))
                .andExpect(status().isOk());

        verify(aiService).getFinancialAdvice(eq(42L), eq("Hello"), anyString());
    }

    @Test
    @DisplayName("Should assemble context from analytics summary")
    @WithMockUser
    void shouldAssembleContextFromAnalytics() throws Exception {
        MonthlySummaryResponse summary = new MonthlySummaryResponse();
        summary.setTotalIncome(BigDecimal.valueOf(100000));
        summary.setTotalExpense(BigDecimal.valueOf(60000));
        summary.setNetSavings(BigDecimal.valueOf(40000));
        when(analyticsService.getMonthlySummary(eq(1L), anyInt(), anyInt())).thenReturn(summary);
        when(aiService.getFinancialAdvice(eq(1L), anyString(), anyString())).thenReturn("Looks good!");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("message", "How am I doing?"))))
                .andExpect(status().isOk());

        // Verify context passed to AI service contains actual summary numbers
        verify(aiService).getFinancialAdvice(eq(1L), eq("How am I doing?"),
                argThat(ctx -> ctx.contains("100000") && ctx.contains("60000") && ctx.contains("40000")));
    }

    @Test
    @DisplayName("Should gracefully handle analytics service failure")
    @WithMockUser
    void shouldHandleAnalyticsFailure() throws Exception {
        when(analyticsService.getMonthlySummary(anyLong(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("DB connection failed"));
        when(aiService.getFinancialAdvice(eq(1L), anyString(), eq("No recent data available.")))
                .thenReturn("I don't have your financial data right now.");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("message", "Show me my budget"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").exists());

        // Context should be the fallback string
        verify(aiService).getFinancialAdvice(eq(1L), eq("Show me my budget"), eq("No recent data available."));
    }
}
