package com.github.sansarch.task_management.infrastructure.shared.web;

import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.infrastructure.shared.web.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Nested
    @DisplayName("handleConstraintViolation()")
    class HandleConstraintViolation {

        @Test
        @DisplayName("should return 400 with the joined violation messages")
        void shouldReturn400() {
            Path propertyPath = mock(Path.class);
            when(propertyPath.toString()).thenReturn("list.size");
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            when(violation.getPropertyPath()).thenReturn(propertyPath);
            when(violation.getMessage()).thenReturn("must be less than or equal to 100");
            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("list.size: must be less than or equal to 100");
        }
    }

    @Nested
    @DisplayName("handleTypeMismatch()")
    class HandleTypeMismatch {

        @Test
        @DisplayName("should return 400 naming the invalid parameter and value")
        void shouldReturn400() {
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("sortBy");
            when(ex.getValue()).thenReturn("bogus");

            ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Invalid value for parameter 'sortBy': bogus");
        }
    }
}
