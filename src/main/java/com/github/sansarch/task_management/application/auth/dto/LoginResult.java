package com.github.sansarch.task_management.application.auth.dto;

import java.util.UUID;

public record LoginResult(String token, UUID userId, String email, long expiresInSeconds) {
}
