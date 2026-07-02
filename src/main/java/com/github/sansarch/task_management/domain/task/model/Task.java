package com.github.sansarch.task_management.domain.task.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {

    private TaskId id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task(TaskId id, String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
