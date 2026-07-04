package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.ListTasksUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import org.springframework.stereotype.Service;

@Service
public class ListTasksService implements ListTasksUseCase {

    private final TaskGateway taskDomainRepository;

    public ListTasksService(TaskGateway taskDomainRepository) {
        this.taskDomainRepository = taskDomainRepository;
    }

    @Override
    public PageResult<TaskResult> list(TaskFilter filter, TaskPageRequest pageRequest) {
        return taskDomainRepository.findAll(filter, pageRequest)
                .map(task -> new TaskResult(
                        task.getId().id(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getPriority(),
                        task.getDueDate(),
                        task.getCreatedAt(),
                        task.getUpdatedAt()
                ));
    }
}
