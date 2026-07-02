package com.github.sansarch.task_management.application.task.port.out;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Optional<Task> findById(TaskId id);
    List<Task> findAll(TaskFilter filter);
    Task save(Task task);
    void delete(TaskId id);
}
