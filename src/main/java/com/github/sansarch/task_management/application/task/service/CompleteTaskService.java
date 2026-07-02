package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.CompleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompleteTaskService implements CompleteTaskUseCase {

    private final TaskGateway taskGateway;

    public CompleteTaskService(TaskGateway taskGateway) {
        this.taskGateway = taskGateway;
    }

    @Override
    public TaskResult complete(UUID id) {
        Task task = taskGateway.findById(new TaskId(id))
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));

        task.complete();

        Task saved = taskGateway.save(task);

        return new TaskResult(
                saved.getId().id(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getStatus(),
                saved.getPriority(),
                saved.getDueDate(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
