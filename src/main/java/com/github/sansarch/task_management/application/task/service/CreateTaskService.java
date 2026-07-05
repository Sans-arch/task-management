package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.application.task.dto.CreateTaskCommand;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.CreateTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

@Service
public class CreateTaskService implements CreateTaskUseCase {

    private final TaskGateway taskDomainRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CreateTaskService(TaskGateway taskDomainRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.taskDomainRepository = taskDomainRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public TaskResult create(CreateTaskCommand command) {
        UserId ownerId = authenticatedUserProvider.getCurrentUserId();

        Task task = Task.create(
                ownerId,
                command.title(),
                command.description(),
                command.status(),
                command.priority(),
                command.dueDate()
        );

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
