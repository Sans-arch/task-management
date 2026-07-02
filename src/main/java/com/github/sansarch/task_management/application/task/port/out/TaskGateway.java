package com.github.sansarch.task_management.application.task.port.out;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.repository.TaskRepository;

import java.util.List;

public interface TaskGateway extends TaskRepository {
    List<Task> findAll(TaskFilter filter);
}
