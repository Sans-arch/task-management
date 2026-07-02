package com.github.sansarch.task_management.application.task.port.in;

import com.github.sansarch.task_management.application.task.dto.TaskResult;

import java.util.UUID;

public interface CompleteTaskUseCase {
    TaskResult complete(UUID id);
}
