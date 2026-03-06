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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
    void setUp() throws Exception {
        // Register a fresh user and get a JWT token
        RegisterRequest reg = new RegisterRequest(
                "Profile User", "profile-" + System.nanoTime() + "@test.com", "Password1!");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void getProfile_authenticated_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Profile User"))
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_changeName_success() throws Exception {
        UpdateProfileRequest update = new UpdateProfileRequest();
        update.setName("Updated Name");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void changePassword_success() throws Exception {
        ChangePasswordRequest cpRequest = new ChangePasswordRequest();
        cpRequest.setCurrentPassword("Password1!");
        cpRequest.setNewPassword("NewPassword2@");

        mockMvc.perform(put("/api/profile/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cpRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_wrongCurrent_returns400() throws Exception {
        ChangePasswordRequest cpRequest = new ChangePasswordRequest();
        cpRequest.setCurrentPassword("WrongPassword1!");
        cpRequest.setNewPassword("NewPassword2@");

        mockMvc.perform(put("/api/profile/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cpRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_weakNewPassword_returns400() throws Exception {
        ChangePasswordRequest cpRequest = new ChangePasswordRequest();
        cpRequest.setCurrentPassword("Password1!");
        cpRequest.setNewPassword("weak"); // Doesn't meet complexity

        mockMvc.perform(put("/api/profile/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cpRequest)))
                .andExpect(status().isBadRequest());
    }
}
