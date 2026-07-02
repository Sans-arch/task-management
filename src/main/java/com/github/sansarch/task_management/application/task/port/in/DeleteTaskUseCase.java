package com.github.sansarch.task_management.application.task.port.in;

import java.util.UUID;

public interface DeleteTaskUseCase {
    void delete(UUID id);
}
