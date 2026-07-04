package com.github.sansarch.task_management.infrastructure.task.adapter.out.persistence;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.SortDirection;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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
    public PageResult<Task> findAll(TaskFilter filter, TaskPageRequest pageRequest) {
        Sort.Direction direction = pageRequest.sortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageRequest.page(), pageRequest.size(), Sort.by(direction, pageRequest.sortBy().fieldName()));

        Page<TaskJpaEntity> page = springDataTaskRepository.findByFilter(filter.status(), filter.priority(), pageable);

        return new PageResult<>(
                page.getContent().stream().map(taskMapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
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
