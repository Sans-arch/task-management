package com.github.sansarch.task_management.infrastructure.task.adapter.in.web;

import com.github.sansarch.task_management.application.task.dto.CreateTaskCommand;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.UpdateTaskCommand;
import com.github.sansarch.task_management.application.task.port.in.CompleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.CreateTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.DeleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.ListTasksUseCase;
import com.github.sansarch.task_management.application.task.port.in.MarkTaskInProgressUseCase;
import com.github.sansarch.task_management.application.task.port.in.UpdateTaskUseCase;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import com.github.sansarch.task_management.infrastructure.task.adapter.in.web.request.TaskRequest;
import com.github.sansarch.task_management.infrastructure.task.adapter.in.web.response.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final MarkTaskInProgressUseCase markTaskInProgressUseCase;
    private final CompleteTaskUseCase completeTaskUseCase;

    public TaskController(CreateTaskUseCase createTaskUseCase, UpdateTaskUseCase updateTaskUseCase,
                          DeleteTaskUseCase deleteTaskUseCase, ListTasksUseCase listTasksUseCase,
                          MarkTaskInProgressUseCase markTaskInProgressUseCase, CompleteTaskUseCase completeTaskUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.listTasksUseCase = listTasksUseCase;
        this.markTaskInProgressUseCase = markTaskInProgressUseCase;
        this.completeTaskUseCase = completeTaskUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestBody @Valid TaskRequest request) {
        return TaskResponse.from(createTaskUseCase.create(new CreateTaskCommand(
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate()
        )));
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) TaskStatus status,
                                   @RequestParam(required = false) TaskPriority priority) {
        return listTasksUseCase.list(new TaskFilter(status, priority))
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @RequestBody @Valid TaskRequest request) {
        return TaskResponse.from(updateTaskUseCase.update(new UpdateTaskCommand(
                id,
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        )));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteTaskUseCase.delete(id);
    }

    @PatchMapping("/{id}/start")
    public TaskResponse markInProgress(@PathVariable UUID id) {
        return TaskResponse.from(markTaskInProgressUseCase.markInProgress(id));
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable UUID id) {
        return TaskResponse.from(completeTaskUseCase.complete(id));
    }
}
