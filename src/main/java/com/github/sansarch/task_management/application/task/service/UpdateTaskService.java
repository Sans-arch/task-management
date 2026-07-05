package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.dto.UpdateTaskCommand;
import com.github.sansarch.task_management.application.task.port.in.UpdateTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.application.task.security.TaskAuthorizationService;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.stereotype.Service;

@Service
public class UpdateTaskService implements UpdateTaskUseCase {

    private final TaskGateway taskDomainRepository;
    private final TaskAuthorizationService taskAuthorizationService;

    public UpdateTaskService(TaskGateway taskDomainRepository, TaskAuthorizationService taskAuthorizationService) {
        this.taskDomainRepository = taskDomainRepository;
        this.taskAuthorizationService = taskAuthorizationService;
    }

    @Override
    public TaskResult update(UpdateTaskCommand command) {
        Task task = taskDomainRepository.findById(new TaskId(command.id()))
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + command.id()));

        taskAuthorizationService.assertCanManage(task);

        task.update(command.title(), command.description(), command.priority(), command.dueDate());

        Task saved = taskDomainRepository.save(task);

        return new TaskResult(
                saved.getId().id(),
                saved.getOwnerId().id(),
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
