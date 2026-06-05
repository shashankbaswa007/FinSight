package com.finsight.controller;

import com.finsight.dto.ImportExternalTransactionsRequest;
import com.finsight.dto.ReconciliationBatchResponse;
import com.finsight.dto.TransactionMatchResponse;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.ExternalTransactionRepository;
import com.finsight.repository.ReconciliationBatchRepository;
import com.finsight.repository.TransactionMatchRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import com.finsight.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ReconciliationIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ExternalTransactionRepository externalTransactionRepository;

    @Autowired
    private ReconciliationBatchRepository reconciliationBatchRepository;

    @Autowired
    private TransactionMatchRepository transactionMatchRepository;

        @MockBean
    private SecurityUtil securityUtil;

    private User testUser;

    @BeforeEach
    public void setup() {
        transactionMatchRepository.deleteAll();
        externalTransactionRepository.deleteAll();
        reconciliationBatchRepository.deleteAll();
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Integration Test")
                .email("itest@example.com")
                .password("pwd")
                .build();
        testUser = userRepository.save(Objects.requireNonNull(testUser, "testUser"));

        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    @org.junit.jupiter.api.AfterEach
    public void teardown() {
        transactionMatchRepository.deleteAll();
        externalTransactionRepository.deleteAll();
        reconciliationBatchRepository.deleteAll();
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
        @SuppressWarnings("unused")
    public void reconciliationEndToEnd_andExportCsv() {
        // create category and internal transaction
        Category cat = Category.builder().name("TestCat").type(TransactionType.EXPENSE).build();
        cat = categoryRepository.save(Objects.requireNonNull(cat, "category"));

        Transaction t = Transaction.builder()
                .user(testUser)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .category(cat)
                .description("internal")
                .date(LocalDate.now())
                .build();
        transactionRepository.save(Objects.requireNonNull(t, "transaction"));

        // create batch
        String batchDate = LocalDate.now().toString();
        try {
            MvcResult res = mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/reconciliation/batches")
                                    .param("batchDate", batchDate)
                    )
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            ReconciliationBatchResponse created = objectMapper.readValue(res.getResponse().getContentAsString(), ReconciliationBatchResponse.class);
            Long batchId = created.getId();

            // import external transaction matching the internal one
            ImportExternalTransactionsRequest.ExternalTransactionItem item = new ImportExternalTransactionsRequest.ExternalTransactionItem();
            item.setExternalId("EXT-1");
            item.setAmount(new BigDecimal("100.00"));
            item.setTransactionDate(LocalDate.now());
            item.setDescription("bank");

            ImportExternalTransactionsRequest payload = new ImportExternalTransactionsRequest();
            payload.setSource("BANK");
            payload.setTransactions(List.of(item));

            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/reconciliation/import-transactions")
                            .contentType(json())
                            .content(toJson(payload))
            ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

            // run reconcile
            mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/reconciliation/batches/" + batchId + "/reconcile")
                            .param("batchDate", batchDate)
            ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

            // fetch matches
            MvcResult matchesRes = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reconciliation/batches/" + batchId + "/matches")
            ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();

            TransactionMatchResponse[] matches = objectMapper.readValue(matchesRes.getResponse().getContentAsString(), TransactionMatchResponse[].class);
            assertThat(matches).isNotNull();
            assertThat(matches.length).isGreaterThanOrEqualTo(1);

            // export CSV
            MvcResult csvRes = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reconciliation/batches/" + batchId + "/export")
                            .param("format", "csv")
            ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();

            String contentType = csvRes.getResponse().getContentType();
            assertThat(contentType).contains("text/csv");
            String csvStr = csvRes.getResponse().getContentAsString();
            assertThat(csvStr).contains("internalTransactionId");
            assertThat(csvStr).contains("EXT-1");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
