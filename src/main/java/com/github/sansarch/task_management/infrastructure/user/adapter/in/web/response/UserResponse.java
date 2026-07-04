package com.github.sansarch.task_management.infrastructure.user.adapter.in.web.response;

import com.github.sansarch.task_management.application.user.dto.UserResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(UserResult result) {
        return new UserResponse(
                result.id(),
                result.email(),
                result.displayName(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
