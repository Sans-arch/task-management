package com.sansarch.task_management.domain.task.service;

import com.sansarch.task_management.domain.task.entity.Task;
import com.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.sansarch.task_management.infra.http.dto.CreateTaskInputDTO;
import com.sansarch.task_management.infra.http.dto.CreateTaskOutputDTO;
import com.sansarch.task_management.infra.http.dto.UpdateTaskInputDTO;
import com.sansarch.task_management.infra.http.dto.UpdateTaskOutputDTO;
import com.sansarch.task_management.infra.mapper.TaskMapper;
import com.sansarch.task_management.infra.repository.TaskRepository;
import com.sansarch.task_management.infra.repository.model.TaskModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public List<Task> listTasks() {
        return taskRepository.findAll().stream().map(model ->
                        Task.builder()
                                .id(model.getId())
                                .title(model.getTitle())
                                .description(model.getDescription())
                                .isCompleted(model.isCompleted())
                                .dueDate(model.getDueDate())
                                .build())
                .toList();
    }

    public CreateTaskOutputDTO createTask(CreateTaskInputDTO input) {
        Task task = Task.builder()
                .title(input.getTitle())
                .description(input.getDescription())
                .dueDate(input.getDueDate())
                .isCompleted(false)
                .build();

        TaskModel persistedTask = taskRepository.save(taskMapper.toModel(task));
        task = taskMapper.toDomain(persistedTask);

        return CreateTaskOutputDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .isCompleted(task.isCompleted())
                .build();
    }

    public Task retrieveTask(Long id) {
        Optional<TaskModel> model = taskRepository.findById(id);

        if (model.isEmpty()) {
            throw new TaskNotFoundException("Task not found!");
        }

        return Task.builder()
                .id(model.get().getId())
                .title(model.get().getTitle())
                .description(model.get().getDescription())
                .isCompleted(model.get().isCompleted())
                .dueDate(model.get().getDueDate())
                .build();
    }

    public UpdateTaskOutputDTO updateTask(Long id, UpdateTaskInputDTO input) {
        Optional<TaskModel> taskModel = taskRepository.findById(id);

        if (taskModel.isEmpty()) {
            throw new TaskNotFoundException("Task not found!");
        }

        Task task = taskMapper.toDomain(taskModel.get());
        task.changeTitle(input.getTitle());
        task.changeDescription(input.getDescription());
        if (input.isCompleted()) {
            task.markAsComplete();
        } else {
            task.markAsIncomplete();
        }

        taskRepository.save(taskMapper.toModel(task));

        return UpdateTaskOutputDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .isCompleted(task.isCompleted())
                .dueDate(task.getDueDate())
                .build();
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
