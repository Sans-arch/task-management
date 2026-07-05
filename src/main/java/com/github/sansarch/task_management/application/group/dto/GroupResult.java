package com.github.sansarch.task_management.application.group.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GroupResult(UUID id, String name, LocalDateTime createdAt, List<UUID> memberIds) {
}
