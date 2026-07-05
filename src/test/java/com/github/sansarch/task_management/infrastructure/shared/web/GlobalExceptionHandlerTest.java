package com.github.sansarch.task_management.infrastructure.shared.web;

import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import com.github.sansarch.task_management.domain.task.exception.TaskAccessDeniedException;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.user.exception.DuplicateEmailException;
import com.github.sansarch.task_management.domain.user.exception.InvalidUserStateException;
import com.github.sansarch.task_management.domain.user.exception.UserNotFoundException;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
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
    @DisplayName("handleTaskAccessDenied()")
    class HandleTaskAccessDenied {

        @Test
        @DisplayName("should return 403 with the exception message")
        void shouldReturn403() {
            TaskAccessDeniedException ex = new TaskAccessDeniedException("User cannot manage task abc");

            ResponseEntity<ErrorResponse> response = handler.handleTaskAccessDenied(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(403);
            assertThat(response.getBody().message()).isEqualTo("User cannot manage task abc");
        }
    }

    @Nested
    @DisplayName("handleUserNotFound()")
    class HandleUserNotFound {

        @Test
        @DisplayName("should return 404 with the exception message")
        void shouldReturn404() {
            UserNotFoundException ex = new UserNotFoundException("User not found: abc");

            ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().message()).isEqualTo("User not found: abc");
        }
    }

    @Nested
    @DisplayName("handleDuplicateEmail()")
    class HandleDuplicateEmail {

        @Test
        @DisplayName("should return 409 with the exception message")
        void shouldReturn409() {
            DuplicateEmailException ex = new DuplicateEmailException("Email already registered: jane@example.com");

            ResponseEntity<ErrorResponse> response = handler.handleDuplicateEmail(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().message()).isEqualTo("Email already registered: jane@example.com");
        }
    }

    @Nested
    @DisplayName("handleInvalidUserState()")
    class HandleInvalidUserState {

        @Test
        @DisplayName("should return 400 with the exception message")
        void shouldReturn400() {
            InvalidUserStateException ex = new InvalidUserStateException("User email must not be blank");

            ResponseEntity<ErrorResponse> response = handler.handleInvalidUserState(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("User email must not be blank");
        }
    }

    @Nested
    @DisplayName("handleValidation()")
    class HandleValidation {

        @Test
        @DisplayName("should return 400 with the joined field error messages")
        void shouldReturn400() {
            FieldError titleError = new FieldError("taskRequest", "title", "must not be blank");
            FieldError priorityError = new FieldError("taskRequest", "priority", "must not be null");
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(titleError, priorityError));
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("must not be blank, must not be null");
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
