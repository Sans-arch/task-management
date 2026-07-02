package com.github.sansarch.task_management.application.task.dto;

import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;

import java.time.LocalDate;

public record CreateTaskCommand(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate) {
}
