package com.github.sansarch.task_management.application.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResult(
        UUID id,
        String email,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
