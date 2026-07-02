package com.github.sansarch.task_management.application.task.port.out;

import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;

import java.util.Optional;

public interface TaskRepository {
    Optional<Task> findById(TaskId id);
    Task save(Task task);
}
