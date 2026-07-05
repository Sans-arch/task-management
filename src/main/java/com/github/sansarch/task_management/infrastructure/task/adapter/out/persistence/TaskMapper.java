package com.github.sansarch.task_management.infrastructure.task.adapter.out.persistence;

import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskJpaEntity toEntity(Task task) {
        return new TaskJpaEntity(
                task.getId().id(),
                task.getOwnerId().id(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public Task toDomain(TaskJpaEntity entity) {
        return Task.reconstitute(
                new TaskId(entity.getId()),
                new UserId(entity.getOwnerId()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getDueDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
