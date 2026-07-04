package com.github.sansarch.task_management.application.task.port.out;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.repository.TaskRepository;

public interface TaskGateway extends TaskRepository {
    PageResult<Task> findAll(TaskFilter filter, TaskPageRequest pageRequest);
}
