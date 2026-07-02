package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.port.in.DeleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskDomainRepository;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteTaskService implements DeleteTaskUseCase {

    private final TaskDomainRepository taskDomainRepository;

    public DeleteTaskService(TaskDomainRepository taskDomainRepository) {
        this.taskDomainRepository = taskDomainRepository;
    }

    @Override
    public void delete(UUID id) {
        TaskId taskId = new TaskId(id);
        taskDomainRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
        taskDomainRepository.delete(taskId);
    }
}
