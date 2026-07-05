package com.github.sansarch.task_management.domain.task.model;

import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import com.github.sansarch.task_management.domain.user.model.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Objects;

import static java.util.Objects.isNull;

public class Task {

    private final TaskId id;
    private final UserId ownerId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Task(TaskId id, UserId ownerId, String title, String description, TaskStatus status, TaskPriority priority,
                 LocalDate dueDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        this.validate();
    }

    public static Task create(UserId ownerId, String title, String description, TaskStatus status, TaskPriority priority,
                              LocalDate dueDate) {
        LocalDateTime now = LocalDateTime.now();
        return new Task(TaskId.generate(), ownerId, title, description, status, priority, dueDate, now, now);
    }

    public static Task reconstitute(TaskId id, UserId ownerId, String title, String description, TaskStatus status,
                                    TaskPriority priority, LocalDate dueDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Task(id, ownerId, title, description, status, priority, dueDate, createdAt, updatedAt);
    }

    public TaskId getId() { return id; }
    public UserId getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public TaskPriority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String title, String description, TaskPriority priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
        this.validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void markInProgress() {
        if (this.status != TaskStatus.TODO) {
            throw new InvalidTaskStateException("Cannot mark task as in progress from status: " + this.status);
        }
        this.status = TaskStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status == TaskStatus.DONE) {
            throw new InvalidTaskStateException("Task is already completed");
        }
        this.status = TaskStatus.DONE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validate() {
        if (isNull(id)) {
            throw new InvalidTaskStateException("Task id must not be null");
        }
        if (isNull(ownerId)) {
            throw new InvalidTaskStateException("Task ownerId must not be null");
        }
        if (isNull(title) || title.isBlank()) {
            throw new InvalidTaskStateException("Task title must not be blank");
        }
        if (title.length() > 255) {
            throw new InvalidTaskStateException("Task title must not exceed 255 characters");
        }
        if (isNull(status)) {
            throw new InvalidTaskStateException("Task status must not be null");
        }
        if (isNull(priority)) {
            throw new InvalidTaskStateException("Task priority must not be null");
        }
        if (isNull(createdAt)) {
            throw new InvalidTaskStateException("Task createdAt must not be null");
        }
        if (isNull(updatedAt)) {
            throw new InvalidTaskStateException("Task updatedAt must not be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new InvalidTaskStateException("Task updatedAt must not be before createdAt");
        }
    }
}
