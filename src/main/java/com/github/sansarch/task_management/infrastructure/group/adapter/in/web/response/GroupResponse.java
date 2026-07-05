package com.github.sansarch.task_management.infrastructure.group.adapter.in.web.response;

import com.github.sansarch.task_management.application.group.dto.GroupResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GroupResponse(UUID id, String name, LocalDateTime createdAt, List<UUID> memberIds) {

    public static GroupResponse from(GroupResult result) {
        return new GroupResponse(result.id(), result.name(), result.createdAt(), result.memberIds());
    }
}
