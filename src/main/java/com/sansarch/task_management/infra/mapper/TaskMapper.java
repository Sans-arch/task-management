package com.sansarch.task_management.infra.mapper;

import com.sansarch.task_management.domain.task.entity.Task;
import com.sansarch.task_management.infra.repository.model.TaskModel;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper implements BaseMapper<Task, TaskModel> {
    public Task toDomain(TaskModel model) {
        return Task.builder()
                .id(model.getId())
                .title(model.getTitle())
                .description(model.getDescription())
                .isCompleted(model.isCompleted())
                .dueDate(model.getDueDate())
                .build();
    }

    public TaskModel toModel(Task domain) {
        return TaskModel.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .isCompleted(domain.isCompleted())
                .dueDate(domain.getDueDate())
                .build();
    }
}
