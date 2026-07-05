package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.port.in.DeleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.application.task.security.TaskAuthorizationService;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteTaskService implements DeleteTaskUseCase {

    private final TaskGateway taskDomainRepository;
    private final TaskAuthorizationService taskAuthorizationService;

    public DeleteTaskService(TaskGateway taskDomainRepository, TaskAuthorizationService taskAuthorizationService) {
        this.taskDomainRepository = taskDomainRepository;
        this.taskAuthorizationService = taskAuthorizationService;
    }

    @Override
    public void delete(UUID id) {
        TaskId taskId = new TaskId(id);
        Task task = taskDomainRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));

        taskAuthorizationService.assertCanManage(task);

        taskDomainRepository.delete(taskId);
    }
}
