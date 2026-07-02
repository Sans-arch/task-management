package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.port.in.DeleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskRepository;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteTaskService implements DeleteTaskUseCase {

    private final TaskRepository taskRepository;

    public DeleteTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void delete(UUID id) {
        TaskId taskId = new TaskId(id);
        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
        taskRepository.delete(taskId);
    }
}
