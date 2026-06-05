package com.finsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.dto.CreateWalletRequest;
import com.finsight.model.Currency;
import com.finsight.model.User;
import com.finsight.model.UserWallet;
import com.finsight.repository.CurrencyRepository;
import com.finsight.repository.UserRepository;
import com.finsight.repository.UserWalletRepository;
import com.finsight.repository.WebhookRepository;
import com.finsight.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Currency and Wallet subsystems (Phase 2).
 * Exercises: currency retrieval, wallet creation, wallet listing, and balance management.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CurrencyIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserWalletRepository userWalletRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WebhookRepository webhookRepository;

        @MockitoBean
    private SecurityUtil securityUtil;

    private User testUser;

        @BeforeEach
        @SuppressWarnings("unused")
    void setup() {
        webhookRepository.deleteAll();
        userWalletRepository.deleteAll();
        userRepository.deleteAll();
        currencyRepository.deleteAll();

        // Seed default currencies
        Currency usd = new Currency("USD", "US Dollar", "$");
        Currency eur = new Currency("EUR", "Euro", "€");
        Currency gbp = new Currency("GBP", "British Pound", "£");
        currencyRepository.save(usd);
        currencyRepository.save(eur);
        currencyRepository.save(gbp);

        // Create test user directly
        testUser = User.builder()
                .name("Currency User")
                .email("currency@test.com")
                .password("pwd")
                .build();
        testUser = userRepository.save(Objects.requireNonNull(testUser, "testUser"));

        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    @Test
        @SuppressWarnings("unused")
    void getActiveCurrencies_success() throws Exception {
        mockMvc.perform(get("/api/v1/currencies")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)) // USD, EUR, GBP seeded
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
        @SuppressWarnings("unused")
    void getPrimaryWallet_createsIfNotExists() throws Exception {
        // Get primary wallet (should auto-create if not exists)
        mockMvc.perform(get("/api/v1/wallets/primary")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode").exists())
                .andExpect(jsonPath("$.balance").value(0));

        // Verify wallet was persisted
        List<UserWallet> wallets = userWalletRepository.findByUserId(testUser.getId());
        assertThat(wallets).isNotEmpty();
    }

    @Test
        @SuppressWarnings("unused")
    void getUserWallets_success() throws Exception {
        // Get primary wallet first
        mockMvc.perform(get("/api/v1/wallets/primary")
                        )
                .andExpect(status().isOk());

        // List all wallets
        mockMvc.perform(get("/api/v1/wallets")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].currencyCode").exists());
    }

    @Test
        @SuppressWarnings("unused")
    void createWallet_success() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setCurrencyCode("EUR");
        request.setWalletName("Euro Account");
        request.setInitialBalance(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currencyCode").value("EUR"))
                .andExpect(jsonPath("$.walletName").value("Euro Account"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.id").isNotEmpty());

        // Verify wallet persisted
        List<UserWallet> wallets = userWalletRepository.findByUserId(testUser.getId());
        assertThat(wallets).hasSize(1);
    }

    @Test
        @SuppressWarnings("unused")
    void createMultipleWallets_differentCurrencies() throws Exception {
        // Create USD wallet
        CreateWalletRequest usdRequest = new CreateWalletRequest();
        usdRequest.setCurrencyCode("USD");
        usdRequest.setWalletName("US Dollar Account");
        usdRequest.setInitialBalance(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(json())
                        .content(toJson(usdRequest)))
                .andExpect(status().isCreated());

        // Create EUR wallet
        CreateWalletRequest eurRequest = new CreateWalletRequest();
        eurRequest.setCurrencyCode("EUR");
        eurRequest.setWalletName("Euro Account");
        eurRequest.setInitialBalance(new BigDecimal("750.50"));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(json())
                        .content(toJson(eurRequest)))
                .andExpect(status().isCreated());

        // Create GBP wallet
        CreateWalletRequest gbpRequest = new CreateWalletRequest();
        gbpRequest.setCurrencyCode("GBP");
        gbpRequest.setWalletName("British Pound Account");
        gbpRequest.setInitialBalance(new BigDecimal("1200.75"));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(json())
                        .content(toJson(gbpRequest)))
                .andExpect(status().isCreated());

        // Verify all three wallets created
        List<UserWallet> wallets = userWalletRepository.findByUserId(testUser.getId());
        assertThat(wallets).hasSize(3);
    }

    @Test
        @SuppressWarnings("unused")
    void walletFlow_endToEnd() throws Exception {
        // Step 1: Verify currencies available
        mockMvc.perform(get("/api/v1/currencies")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // Step 2: Get primary wallet (auto-create)
        MvcResult primaryResult = mockMvc.perform(get("/api/v1/wallets/primary")
                        )
                .andExpect(status().isOk())
                .andReturn();

        String primaryCurrency = objectMapper.readTree(primaryResult.getResponse().getContentAsString())
                .get("currencyCode").asText();
        assertThat(primaryCurrency).isNotEmpty();

        // Step 3: List wallets (should have primary only)
        mockMvc.perform(get("/api/v1/wallets")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Step 4: Create additional wallets
        CreateWalletRequest request = new CreateWalletRequest();
        request.setCurrencyCode("EUR");
        request.setWalletName("Secondary Euro");
        request.setInitialBalance(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated());

        // Step 5: List wallets (should have primary + EUR)
        mockMvc.perform(get("/api/v1/wallets")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

        @NonNull
        private MediaType json() {
                return Objects.requireNonNull(MediaType.APPLICATION_JSON, "applicationJson");
        }

        @NonNull
        private String toJson(Object value) throws Exception {
                return Objects.requireNonNull(objectMapper.writeValueAsString(value), "json");
        }
}
