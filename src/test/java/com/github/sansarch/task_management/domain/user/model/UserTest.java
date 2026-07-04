package com.github.sansarch.task_management.domain.user.model;

import com.github.sansarch.task_management.domain.user.exception.InvalidUserStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User")
class UserTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create user with correct fields")
        void shouldCreateUserWithCorrectFields() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");

            assertThat(user.getEmail()).isEqualTo("jane@example.com");
            assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(user.getDisplayName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("should generate id and timestamps automatically")
        void shouldGenerateIdAndTimestampsAutomatically() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");

            assertThat(user.getId()).isNotNull();
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
            assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        @DisplayName("should reconstitute with all provided fields")
        void shouldReconstituteWithAllProvidedFields() {
            UserId id = UserId.generate();
            LocalDateTime createdAt = FIXED_DATETIME;
            LocalDateTime updatedAt = FIXED_DATETIME.plusDays(1);

            User user = User.reconstitute(id, "jane@example.com", "hashed-password", "Jane Doe", createdAt, updatedAt);

            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getEmail()).isEqualTo("jane@example.com");
            assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(user.getDisplayName()).isEqualTo("Jane Doe");
            assertThat(user.getCreatedAt()).isEqualTo(createdAt);
            assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("should throw when email is null")
        void shouldThrowWhenEmailIsNull() {
            assertThatThrownBy(() -> User.create(null, "hashed-password", "Jane Doe"))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("should throw when email is blank")
        void shouldThrowWhenEmailIsBlank() {
            assertThatThrownBy(() -> User.create("   ", "hashed-password", "Jane Doe"))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("should throw when email exceeds 255 characters")
        void shouldThrowWhenEmailExceeds255Characters() {
            String longEmail = "a".repeat(250) + "@a.com";

            assertThatThrownBy(() -> User.create(longEmail, "hashed-password", "Jane Doe"))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("255");
        }

        @Test
        @DisplayName("should throw when email does not contain @")
        void shouldThrowWhenEmailIsMissingAtSign() {
            assertThatThrownBy(() -> User.create("not-an-email", "hashed-password", "Jane Doe"))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("should throw when passwordHash is blank")
        void shouldThrowWhenPasswordHashIsBlank() {
            assertThatThrownBy(() -> User.create("jane@example.com", "  ", "Jane Doe"))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("passwordHash");
        }

        @Test
        @DisplayName("should throw when displayName is blank")
        void shouldThrowWhenDisplayNameIsBlank() {
            assertThatThrownBy(() -> User.create("jane@example.com", "hashed-password", " "))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("displayName");
        }

        @Test
        @DisplayName("should throw when displayName exceeds 255 characters")
        void shouldThrowWhenDisplayNameExceeds255Characters() {
            String longName = "a".repeat(256);

            assertThatThrownBy(() -> User.create("jane@example.com", "hashed-password", longName))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("255");
        }

        @Test
        @DisplayName("should throw when updatedAt is before createdAt")
        void shouldThrowWhenUpdatedAtIsBeforeCreatedAt() {
            LocalDateTime createdAt = FIXED_DATETIME;
            LocalDateTime updatedAt = FIXED_DATETIME.minusSeconds(1);

            assertThatThrownBy(() -> User.reconstitute(UserId.generate(), "jane@example.com", "hashed-password",
                    "Jane Doe", createdAt, updatedAt))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("updatedAt");
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class Equality {

        @Test
        @DisplayName("should be equal when same id")
        void shouldBeEqualWhenSameId() {
            UserId id = UserId.generate();
            User u1 = User.reconstitute(id, "a@example.com", "hash-a", "A", FIXED_DATETIME, FIXED_DATETIME);
            User u2 = User.reconstitute(id, "b@example.com", "hash-b", "B", FIXED_DATETIME, FIXED_DATETIME);

            assertThat(u1).isEqualTo(u2);
            assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when different id")
        void shouldNotBeEqualWhenDifferentId() {
            User u1 = User.reconstitute(UserId.generate(), "a@example.com", "hash-a", "A", FIXED_DATETIME, FIXED_DATETIME);
            User u2 = User.reconstitute(UserId.generate(), "a@example.com", "hash-a", "A", FIXED_DATETIME, FIXED_DATETIME);

            assertThat(u1).isNotEqualTo(u2);
        }
    }
}
