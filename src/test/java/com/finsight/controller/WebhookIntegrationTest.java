package com.finsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.dto.CreateWebhookRequest;
import com.finsight.model.User;
import com.finsight.model.Webhook;
import com.finsight.repository.WebhookRepository;
import com.finsight.repository.UserRepository;
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

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Webhook subsystem (Phase 2).
 * Exercises: webhook creation, retrieval, update, deletion, and retry logic.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class WebhookIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebhookRepository webhookRepository;
    @Autowired private UserRepository userRepository;

        @MockitoBean
    private SecurityUtil securityUtil;

    private User testUser;

        @BeforeEach
        @SuppressWarnings("unused")
    void setup() {
        webhookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user directly
        testUser = User.builder()
                .name("Webhook User")
                .email("webhook@test.com")
                .password("pwd")
                .build();
        testUser = userRepository.save(Objects.requireNonNull(testUser, "testUser"));

        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    @Test
        @SuppressWarnings("unused")
    void createWebhook_success() throws Exception {
        CreateWebhookRequest request = new CreateWebhookRequest();
        request.setUrl("https://example.com/webhook");
        request.setEventTypes("[\"transaction.created\", \"transaction.updated\"]");
        request.setRetryCount(3);

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://example.com/webhook"))
                .andExpect(jsonPath("$.retryCount").value(3))
                .andExpect(jsonPath("$.id").isNotEmpty());

        // Verify webhook persisted
        List<Webhook> webhooks = webhookRepository.findByUserId(testUser.getId());
        assertThat(webhooks).hasSize(1);
        assertThat(webhooks.get(0).getUrl()).isEqualTo("https://example.com/webhook");
    }

    @Test
        @SuppressWarnings("unused")
    void getUserWebhooks_success() throws Exception {
        // Create two webhooks
        CreateWebhookRequest request1 = new CreateWebhookRequest();
        request1.setUrl("https://example.com/webhook1");
        request1.setEventTypes("[\"transaction.created\"]");
        request1.setRetryCount(2);

        CreateWebhookRequest request2 = new CreateWebhookRequest();
        request2.setUrl("https://example.com/webhook2");
        request2.setEventTypes("[\"transaction.deleted\"]");
        request2.setRetryCount(1);

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(request2)))
                .andExpect(status().isCreated());

        // Retrieve webhooks
        mockMvc.perform(get("/api/v1/webhooks")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].url").value("https://example.com/webhook1"))
                .andExpect(jsonPath("$[1].url").value("https://example.com/webhook2"));
    }

    @Test
        @SuppressWarnings("unused")
    void getWebhookById_success() throws Exception {
        // Create webhook
        CreateWebhookRequest request = new CreateWebhookRequest();
        request.setUrl("https://example.com/webhook");
        request.setEventTypes("[\"transaction.created\"]");
        request.setRetryCount(2);

        MvcResult result = mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long webhookId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // Retrieve by ID
        mockMvc.perform(get("/api/v1/webhooks/" + webhookId)
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(webhookId))
                .andExpect(jsonPath("$.url").value("https://example.com/webhook"));
    }

    @Test
        @SuppressWarnings("unused")
    void updateWebhook_success() throws Exception {
        // Create webhook
        CreateWebhookRequest createRequest = new CreateWebhookRequest();
        createRequest.setUrl("https://example.com/webhook");
        createRequest.setEventTypes("[\"transaction.created\"]");
        createRequest.setRetryCount(2);

        MvcResult createResult = mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long webhookId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Update webhook
        CreateWebhookRequest updateRequest = new CreateWebhookRequest();
        updateRequest.setUrl("https://updated.com/webhook");
        updateRequest.setEventTypes("[\"transaction.updated\", \"transaction.deleted\"]");
        updateRequest.setRetryCount(5);

        mockMvc.perform(put("/api/v1/webhooks/" + webhookId)
                        .contentType(json())
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://updated.com/webhook"));

        // Verify update persisted
        List<Webhook> webhooks = webhookRepository.findByUserId(testUser.getId());
        assertThat(webhooks.get(0).getUrl()).isEqualTo("https://updated.com/webhook");
    }

    @Test
        @SuppressWarnings("unused")
    void deleteWebhook_success() throws Exception {
        // Create webhook
        CreateWebhookRequest request = new CreateWebhookRequest();
        request.setUrl("https://example.com/webhook");
        request.setEventTypes("[\"transaction.created\"]");
        request.setRetryCount(2);

        MvcResult result = mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long webhookId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // Delete webhook
        mockMvc.perform(delete("/api/v1/webhooks/" + webhookId)
                        )
                .andExpect(status().isNoContent());
    }

    @Test
        @SuppressWarnings("unused")
    void webhookFlow_endToEnd() throws Exception {
        // Create webhook with high retry count for long-running deliveries
        CreateWebhookRequest createRequest = new CreateWebhookRequest();
        createRequest.setUrl("https://api.example.com/events");
        createRequest.setEventTypes("[\"transaction.created\", \"transaction.updated\", \"reconciliation.completed\"]");
        createRequest.setRetryCount(5);

        MvcResult createResult = mockMvc.perform(post("/api/v1/webhooks")
                        .contentType(json())
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long webhookId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // List webhooks
        mockMvc.perform(get("/api/v1/webhooks")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Update webhook to add more events
        CreateWebhookRequest updateRequest = new CreateWebhookRequest();
        updateRequest.setUrl("https://api.example.com/events");
        updateRequest.setEventTypes("[\"transaction.created\", \"transaction.updated\", \"reconciliation.completed\", \"export.completed\"]");
        updateRequest.setRetryCount(5);

        mockMvc.perform(put("/api/v1/webhooks/" + webhookId)
                        .contentType(json())
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk());

        // Delete webhook
        mockMvc.perform(delete("/api/v1/webhooks/" + webhookId)
                        )
                .andExpect(status().isNoContent());

        // Verify empty
        mockMvc.perform(get("/api/v1/webhooks")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
