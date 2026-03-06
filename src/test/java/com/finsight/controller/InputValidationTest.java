package com.finsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.dto.RegisterRequest;
import com.finsight.dto.TransactionRequest;
import com.finsight.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for request validation, edge cases, and boundary conditions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InputValidationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "Validation User", "validation-" + System.nanoTime() + "@test.com", "Password1!");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // ──── Registration Validation ────

    @Test
    void register_blankName_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("", "blank@test.com", "Password1!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordWithoutSpecialChar_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("NoSpecial", "nospec@test.com", "Password1");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordWithoutUppercase_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("NoUpper", "noupper@test.com", "password1!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordWithoutDigit_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("NoDigit", "nodigit@test.com", "Password!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ──── Transaction Validation ────

    @Test
    void createTransaction_negativeAmount_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(-100));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_zeroAmount_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.ZERO);
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_nullType_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(null);
        request.setCategoryId(1L);
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_nullDate_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDate(null);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_nullCategoryId_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(null);
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_descriptionTooLong_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDate(LocalDate.now());
        request.setDescription("A".repeat(501)); // Max is 500

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_invalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
