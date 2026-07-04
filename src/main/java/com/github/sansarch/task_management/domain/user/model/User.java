package com.github.sansarch.task_management.domain.user.model;

import com.github.sansarch.task_management.domain.user.exception.InvalidUserStateException;

import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Objects.isNull;

public class User {

    private final UserId id;
    private String email;
    private String passwordHash;
    private String displayName;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(UserId id, String email, String passwordHash, String displayName,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        this.validate();
    }

    public static User create(String email, String passwordHash, String displayName) {
        LocalDateTime now = LocalDateTime.now();
        return new User(UserId.generate(), email, passwordHash, displayName, now, now);
    }

    public static User reconstitute(UserId id, String email, String passwordHash, String displayName,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(id, email, passwordHash, displayName, createdAt, updatedAt);
    }

    public UserId getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void validate() {
        if (isNull(id)) {
            throw new InvalidUserStateException("User id must not be null");
        }
        if (isNull(email) || email.isBlank()) {
            throw new InvalidUserStateException("User email must not be blank");
        }
        if (email.length() > 255) {
            throw new InvalidUserStateException("User email must not exceed 255 characters");
        }
        if (!email.contains("@")) {
            throw new InvalidUserStateException("User email must be a valid email address");
        }
        if (isNull(passwordHash) || passwordHash.isBlank()) {
            throw new InvalidUserStateException("User passwordHash must not be blank");
        }
        if (isNull(displayName) || displayName.isBlank()) {
            throw new InvalidUserStateException("User displayName must not be blank");
        }
        if (displayName.length() > 255) {
            throw new InvalidUserStateException("User displayName must not exceed 255 characters");
        }
        if (isNull(createdAt)) {
            throw new InvalidUserStateException("User createdAt must not be null");
        }
        if (isNull(updatedAt)) {
            throw new InvalidUserStateException("User updatedAt must not be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new InvalidUserStateException("User updatedAt must not be before createdAt");
        }
    }
}
