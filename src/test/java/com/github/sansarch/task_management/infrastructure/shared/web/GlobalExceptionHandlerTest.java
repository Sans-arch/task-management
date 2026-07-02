package com.github.sansarch.task_management.infrastructure.shared.web;

import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.infrastructure.shared.web.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("handleTaskNotFound()")
    class HandleTaskNotFound {

        @Test
        @DisplayName("should return 404 with the exception message")
        void shouldReturn404() {
            TaskNotFoundException ex = new TaskNotFoundException("Task not found: abc");

            ResponseEntity<ErrorResponse> response = handler.handleTaskNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().message()).isEqualTo("Task not found: abc");
        }
    }

    @Nested
    @DisplayName("handleInvalidTaskState()")
    class HandleInvalidTaskState {

        @Test
        @DisplayName("should return 400 with the exception message")
        void shouldReturn400() {
            InvalidTaskStateException ex = new InvalidTaskStateException("Task title must not be blank");

            ResponseEntity<ErrorResponse> response = handler.handleInvalidTaskState(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Task title must not be blank");
        }
    }

    @Nested
    @DisplayName("handleNotReadable()")
    class HandleNotReadable {

        @Test
        @DisplayName("should return 400 with a fixed message")
        void shouldReturn400() {
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

            ResponseEntity<ErrorResponse> response = handler.handleNotReadable(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Malformed or unreadable JSON");
        }
    }
}
