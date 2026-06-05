package com.finsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.model.Notification;
import com.finsight.model.User;
import com.finsight.repository.NotificationRepository;
import com.finsight.repository.UserRepository;
import com.finsight.repository.UserWalletRepository;
import com.finsight.repository.WebhookRepository;
import com.finsight.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Notification subsystem (Phase 2).
 * Exercises: notification retrieval, marking as read, unread count, and pagination.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
        @Autowired private UserWalletRepository userWalletRepository;
    @Autowired private WebhookRepository webhookRepository;

        @MockBean
    private SecurityUtil securityUtil;

    private User testUser;

        @BeforeEach
        @SuppressWarnings("unused")
    void setup() {
        webhookRepository.deleteAll();
        notificationRepository.deleteAll();
                userWalletRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user directly
        testUser = User.builder()
                .name("Notification User")
                .email("notif@test.com")
                .password("pwd")
                .build();
        testUser = userRepository.save(Objects.requireNonNull(testUser, "testUser"));

        when(securityUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    @Test
        @SuppressWarnings("unused")
    void getUnreadNotifications_whenEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/unread")
                        
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
        @SuppressWarnings("unused")
    void getUnreadCount_whenEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
        @SuppressWarnings("unused")
    void getAllNotifications_success() throws Exception {
        // Create some test notifications
        createTestNotifications(5);

        mockMvc.perform(get("/api/v1/notifications")
                        
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
        @SuppressWarnings("unused")
    void getUnreadNotifications_success() throws Exception {
        // Create some test notifications
        createTestNotifications(3);

        mockMvc.perform(get("/api/v1/notifications/unread")
                        
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
        @SuppressWarnings("unused")
    void getUnreadCount_success() throws Exception {
        // Create some test notifications
        createTestNotifications(5);

        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }

    @Test
        @SuppressWarnings("unused")
    void markAsRead_success() throws Exception {
        // Create a test notification
        Notification notification = new Notification();
        notification.setUser(testUser);
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test");
        notification.setType(Notification.NotificationType.SYSTEM);
        notification.setRead(false);
        notificationRepository.save(notification);

        Long notificationId = notification.getId();

        // Mark as read
        mockMvc.perform(put("/api/v1/notifications/" + notificationId + "/read")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        // Verify in database
        Notification updated = notificationRepository
                .findById(Objects.requireNonNull(notificationId, "notificationId"))
                .orElseThrow();
        assertThat(updated.getRead()).isTrue();
    }

    @Test
        @SuppressWarnings("unused")
    void unreadCount_decreasesAfterMarkAsRead() throws Exception {
        // Create test notifications
        createTestNotifications(3);

        // Verify initial unread count
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));

        // Get notifications
        MvcResult getResult = mockMvc.perform(get("/api/v1/notifications/unread")
                        
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        Long firstNotificationId = objectMapper.readTree(getResult.getResponse().getContentAsString())
                .get("content").get(0).get("id").asLong();

        // Mark one as read
        mockMvc.perform(put("/api/v1/notifications/" + firstNotificationId + "/read")
                        )
                .andExpect(status().isOk());

        // Verify unread count decreased
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));
    }

    @Test
        @SuppressWarnings("unused")
    void notificationFlow_endToEnd() throws Exception {
        // Step 1: Verify empty notifications
        mockMvc.perform(get("/api/v1/notifications")
                        
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        // Step 2: Create test notifications
        createTestNotifications(10);

        // Step 3: Verify unread count
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(10));

        // Step 4: Get unread notifications (paginated)
        MvcResult firstPageResult = mockMvc.perform(get("/api/v1/notifications/unread")
                        
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andReturn();

        // Step 5: Mark first notification as read
        Long firstNotificationId = objectMapper.readTree(firstPageResult.getResponse().getContentAsString())
                .get("content").get(0).get("id").asLong();

        mockMvc.perform(put("/api/v1/notifications/" + firstNotificationId + "/read")
                        )
                .andExpect(status().isOk());

        // Step 6: Verify unread count decreased
        mockMvc.perform(get("/api/v1/notifications/unread/count")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(9));

        // Step 7: Get all notifications (some read, some unread)
        mockMvc.perform(get("/api/v1/notifications")
                        
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10));
    }

    private void createTestNotifications(int count) {
        for (int i = 0; i < count; i++) {
            Notification notification = new Notification();
            notification.setUser(testUser);
            notification.setTitle("Notification " + i);
            notification.setMessage("Message " + i);
            notification.setType(Notification.NotificationType.SYSTEM);
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }
}
