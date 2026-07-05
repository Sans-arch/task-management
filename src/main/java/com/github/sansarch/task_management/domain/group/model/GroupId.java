package com.github.sansarch.task_management.domain.group.model;

import java.util.UUID;

public record GroupId(UUID id) {

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }
}
