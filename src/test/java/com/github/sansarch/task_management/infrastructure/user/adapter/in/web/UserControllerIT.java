package com.github.sansarch.task_management.infrastructure.user.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.github.sansarch.task_management.application.user.dto.UserResult;
import com.github.sansarch.task_management.application.user.port.in.RegisterUserUseCase;
import com.github.sansarch.task_management.domain.user.exception.DuplicateEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("UserController")
class UserControllerIT {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @Nested
    @DisplayName("POST /api/users")
    class Register {

        @Test
        @DisplayName("should return 201 with user response")
        void shouldReturn201() throws Exception {
            when(registerUserUseCase.register(any()))
                    .thenReturn(new UserResult(USER_ID, "jane@example.com", "Jane Doe", FIXED_DATETIME, FIXED_DATETIME));

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", "jane@example.com",
                                    "password", "password123",
                                    "displayName", "Jane Doe"
                            ))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.email").value("jane@example.com"))
                    .andExpect(jsonPath("$.displayName").value("Jane Doe"));
        }

        @Test
        @DisplayName("should return 400 when email is invalid")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", "not-an-email",
                                    "password", "password123",
                                    "displayName", "Jane Doe"
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when password is too short")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", "jane@example.com",
                                    "password", "short",
                                    "displayName", "Jane Doe"
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when displayName is blank")
        void shouldReturn400WhenDisplayNameIsBlank() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", "jane@example.com",
                                    "password", "password123",
                                    "displayName", ""
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 409 when email is already registered")
        void shouldReturn409WhenEmailAlreadyRegistered() throws Exception {
            doThrow(new DuplicateEmailException("Email already registered: jane@example.com"))
                    .when(registerUserUseCase).register(any());

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", "jane@example.com",
                                    "password", "password123",
                                    "displayName", "Jane Doe"
                            ))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }
    }
}
