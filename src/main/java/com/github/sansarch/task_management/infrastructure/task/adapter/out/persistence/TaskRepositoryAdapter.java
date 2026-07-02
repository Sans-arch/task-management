package com.github.sansarch.task_management.infrastructure.task.adapter.out.persistence;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TaskRepositoryAdapter implements TaskGateway {

    private final SpringDataTaskRepository springDataTaskRepository;
    private final TaskMapper taskMapper;

    public TaskRepositoryAdapter(SpringDataTaskRepository springDataTaskRepository, TaskMapper taskMapper) {
        this.springDataTaskRepository = springDataTaskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return springDataTaskRepository.findById(id.id()).map(taskMapper::toDomain);
    }

    @Override
    public List<Task> findAll(TaskFilter filter) {
        return springDataTaskRepository.findByFilter(filter.status(), filter.priority())
                .stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public Task save(Task task) {
        TaskJpaEntity saved = springDataTaskRepository.save(taskMapper.toEntity(task));
        return taskMapper.toDomain(saved);
    }

    @Override
    public void delete(TaskId id) {
        springDataTaskRepository.deleteById(id.id());
    }
}
