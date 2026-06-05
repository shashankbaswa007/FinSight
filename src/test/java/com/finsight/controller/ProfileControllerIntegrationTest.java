package com.finsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.dto.ChangePasswordRequest;
import com.finsight.dto.RegisterRequest;
import com.finsight.dto.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        // Register a fresh user and get a JWT token
        RegisterRequest reg = new RegisterRequest(
                "Profile User", "profile-" + System.nanoTime() + "@test.com", "Password1!");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(json())
                .content(toJson(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @SuppressWarnings("unused")
    void getProfile_authenticated_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Profile User"))
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @SuppressWarnings("unused")
    void getProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SuppressWarnings("unused")
    void updateProfile_changeName_success() throws Exception {
        UpdateProfileRequest update = new UpdateProfileRequest();
        update.setName("Updated Name");

        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                .contentType(json())
                .content(toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @SuppressWarnings("unused")
    void changePassword_success() throws Exception {
        ChangePasswordRequest cpRequest = new ChangePasswordRequest();
        cpRequest.setCurrentPassword("Password1!");
        cpRequest.setNewPassword("NewPassword2@");

        mockMvc.perform(put("/api/v1/profile/password")
                        .header("Authorization", "Bearer " + token)
                .contentType(json())
                .content(toJson(cpRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @SuppressWarnings("unused")
    void changePassword_wrongCurrent_returns400() throws Exception {
        ChangePasswordRequest cpRequest = new ChangePasswordRequest();
        cpRequest.setCurrentPassword("WrongPassword1!");
        cpRequest.setNewPassword("NewPassword2@");

        mockMvc.perform(put("/api/v1/profile/password")
                        .header("Authorization", "Bearer " + token)
                .contentType(json())
                .content(toJson(cpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SuppressWarnings("unused")
    void changePassword_weakNewPassword_returns400() throws Exception {
        ChangePasswordRequest cpRequest = new ChangePasswordRequest();
        cpRequest.setCurrentPassword("Password1!");
        cpRequest.setNewPassword("weak"); // Doesn't meet complexity

        mockMvc.perform(put("/api/v1/profile/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(json())
                        .content(toJson(cpRequest)))
                .andExpect(status().isBadRequest());
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
