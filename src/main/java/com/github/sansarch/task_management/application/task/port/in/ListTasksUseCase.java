package com.github.sansarch.task_management.application.task.port.in;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskResult;

import java.util.List;

public interface ListTasksUseCase {
    List<TaskResult> list(TaskFilter filter);
}
