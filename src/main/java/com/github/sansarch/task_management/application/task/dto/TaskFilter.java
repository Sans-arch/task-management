package com.github.sansarch.task_management.application.task.dto;

import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;

public record TaskFilter(TaskStatus status, TaskPriority priority) {
}
