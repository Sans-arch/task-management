package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.CreateTaskCommand;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.CreateTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskDomainRepository;
import com.github.sansarch.task_management.domain.task.model.Task;
import org.springframework.stereotype.Service;

@Service
public class CreateTaskService implements CreateTaskUseCase {

    private final TaskDomainRepository taskDomainRepository;

    public CreateTaskService(TaskDomainRepository taskDomainRepository) {
        this.taskDomainRepository = taskDomainRepository;
    }

    @Override
    public TaskResult create(CreateTaskCommand command) {
        Task task = Task.create(
                command.title(),
                command.description(),
                command.status(),
                command.priority(),
                command.dueDate()
        );

        Task saved = taskDomainRepository.save(task);

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
