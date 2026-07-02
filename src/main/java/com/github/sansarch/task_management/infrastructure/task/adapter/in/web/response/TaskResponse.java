package com.github.sansarch.task_management.infrastructure.task.adapter.in.web.response;

import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(TaskResult result) {
        return new TaskResponse(
                result.id(),
                result.title(),
                result.description(),
                result.status(),
                result.priority(),
                result.dueDate(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
