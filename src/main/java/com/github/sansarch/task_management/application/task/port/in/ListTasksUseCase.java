package com.github.sansarch.task_management.application.task.port.in;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.dto.TaskResult;

public interface ListTasksUseCase {
    PageResult<TaskResult> list(TaskFilter filter, TaskPageRequest pageRequest);
}
