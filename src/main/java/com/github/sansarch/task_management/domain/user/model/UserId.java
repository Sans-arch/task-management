package com.github.sansarch.task_management.domain.user.model;

import java.util.UUID;

public record UserId(UUID id) {

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
