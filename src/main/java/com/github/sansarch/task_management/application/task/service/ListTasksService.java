package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.ListTasksUseCase;
import com.github.sansarch.task_management.application.task.port.out.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTasksService implements ListTasksUseCase {

    private final TaskRepository taskRepository;

    public ListTasksService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<TaskResult> list(TaskFilter filter) {
        return taskRepository.findAll(filter).stream()
                .map(task -> new TaskResult(
                        task.getId().id(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getPriority(),
                        task.getDueDate(),
                        task.getCreatedAt(),
                        task.getUpdatedAt()
                ))
                .toList();
    }
}
