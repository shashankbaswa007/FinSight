package com.finsight.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import com.finsight.dto.LoginRequest;
import com.finsight.dto.RegisterRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("unused")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
        @SuppressWarnings("unused")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "john@test.com", "Password1!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
        @SuppressWarnings("unused")
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane", "dupe@test.com", "Password1!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isCreated());

        // Attempt duplicate
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
        @SuppressWarnings("unused")
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Bad", "not-an-email", "Password1!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
        @SuppressWarnings("unused")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Short", "short@test.com", "123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
        @SuppressWarnings("unused")
    void login_success() throws Exception {
        // First register
        RegisterRequest reg = new RegisterRequest("Login User", "loginuser@test.com", "Password1!");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(reg)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest login = new LoginRequest();
        login.setEmail("loginuser@test.com");
        login.setPassword("Password1!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(json())
                        .content(toJson(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
        @SuppressWarnings("unused")
    void login_wrongPassword_returns401() throws Exception {
        // First register
        RegisterRequest reg = new RegisterRequest("WrongPw", "wrongpw@test.com", "Password1!");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(json())
                        .content(toJson(reg)))
                .andExpect(status().isCreated());

        // Wrong password
        LoginRequest login = new LoginRequest();
        login.setEmail("wrongpw@test.com");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(json())
                        .content(toJson(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
        @SuppressWarnings("unused")
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                                                .contentType(json())
                        .content("{}"))
                .andExpect(status().isUnauthorized());
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
