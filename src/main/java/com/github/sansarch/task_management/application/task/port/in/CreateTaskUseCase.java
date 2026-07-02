package com.github.sansarch.task_management.application.task.port.in;

import com.github.sansarch.task_management.application.task.dto.CreateTaskCommand;
import com.github.sansarch.task_management.application.task.dto.TaskResult;

public interface CreateTaskUseCase {
    TaskResult create(CreateTaskCommand command);
}
