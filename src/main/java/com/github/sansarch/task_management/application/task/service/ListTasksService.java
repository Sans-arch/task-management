package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.ListTasksUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.application.task.security.TaskAuthorizationService;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ListTasksService implements ListTasksUseCase {

    private final TaskGateway taskDomainRepository;
    private final TaskAuthorizationService taskAuthorizationService;

    public ListTasksService(TaskGateway taskDomainRepository, TaskAuthorizationService taskAuthorizationService) {
        this.taskDomainRepository = taskDomainRepository;
        this.taskAuthorizationService = taskAuthorizationService;
    }

    @Override
    public PageResult<TaskResult> list(TaskFilter filter, TaskPageRequest pageRequest) {
        Set<UserId> visibleOwnerIds = taskAuthorizationService.manageableOwnerIds();

        return taskDomainRepository.findAll(filter, pageRequest, visibleOwnerIds)
                .map(task -> new TaskResult(
                        task.getId().id(),
                        task.getOwnerId().id(),
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
