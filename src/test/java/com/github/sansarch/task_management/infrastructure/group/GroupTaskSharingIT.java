package com.github.sansarch.task_management.infrastructure.group;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Group-based task sharing")
class GroupTaskSharingIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("should let group co-members fully manage each other's tasks, but not an outsider")
    void shouldShareTaskManagementWithinGroup() throws Exception {
        String emailA = "a-" + System.nanoTime() + "@example.com";
        String emailB = "b-" + System.nanoTime() + "@example.com";
        String emailC = "c-" + System.nanoTime() + "@example.com";
        String tokenA = registerAndLogin(emailA);
        String tokenB = registerAndLogin(emailB);
        String tokenC = registerAndLogin(emailC);

        String groupResponse = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Shared Group"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String groupId = objectMapper.readTree(groupResponse).get("id").asText();

        mockMvc.perform(post("/api/groups/{id}/members", groupId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberEmail", emailB))))
                .andExpect(status().isNoContent());

        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "A's task",
                                "status", "TODO",
                                "priority", "HIGH"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String taskId = objectMapper.readTree(taskResponse).get("id").asText();

        // B shares a group with A: can view, edit, and delete A's task.
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + taskId + "')]").isNotEmpty());

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Edited by B",
                                "status", "TODO",
                                "priority", "MEDIUM"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Edited by B"));

        // C shares no group with A: still forbidden and doesn't see the task.
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + tokenC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Hijack",
                                "status", "TODO",
                                "priority", "LOW"
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + tokenC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + taskId + "')]").isEmpty());

        mockMvc.perform(delete("/api/tasks/{id}", taskId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNoContent());
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "password123",
                                "displayName", "Test User"
                        ))))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResponse).get("token").asText();
    }
}
