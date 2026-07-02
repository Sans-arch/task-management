package com.github.sansarch.task_management.domain.task.model;

import java.util.UUID;

public record TaskId(UUID id) {

    public static TaskId generate() {
        return new TaskId(UUID.randomUUID());
    }
}
