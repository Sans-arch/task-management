package com.github.sansarch.task_management.application.task.dto;

import com.github.sansarch.task_management.domain.task.model.TaskPriority;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskCommand(UUID id, String title, String description, TaskPriority priority, LocalDate dueDate) {
}
