package com.github.sansarch.task_management.application.task.port.in;

import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.dto.UpdateTaskCommand;

public interface UpdateTaskUseCase {
    TaskResult update(UpdateTaskCommand command);
}
