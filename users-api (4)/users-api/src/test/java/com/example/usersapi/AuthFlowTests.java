package com.example.usersapi;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenAccessOwnProfile_succeeds() throws Exception {
        Map<String, Object> registerBody = Map.of(
                "name", "Carol",
                "email", "carol@example.com",
                "password", "password123",
                "age", 28
        );

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carol@example.com"));
    }

    @Test
    void accessProtectedRouteWithoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotAccessAdminUsersEndpoint() throws Exception {
        Map<String, Object> registerBody = Map.of(
                "name", "Dave",
                "email", "dave@example.com",
                "password", "password123",
                "age", 22
        );

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // Regular USER role should be forbidden from the admin-only /api/users endpoint
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithCorrectPassword_returnsToken() throws Exception {
        Map<String, Object> registerBody = Map.of(
                "name", "Erin",
                "email", "erin@example.com",
                "password", "correctPassword1",
                "age", 33
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        Map<String, Object> loginBody = Map.of(
                "email", "erin@example.com",
                "password", "correctPassword1"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("erin@example.com"));
    }

    @Test
    void loginWithWrongPassword_returns401() throws Exception {
        Map<String, Object> registerBody = Map.of(
                "name", "Finn",
                "email", "finn@example.com",
                "password", "correctPassword1",
                "age", 33
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        Map<String, Object> loginBody = Map.of(
                "email", "finn@example.com",
                "password", "wrongPassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized());
    }
}
