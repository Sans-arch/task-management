package com.github.sansarch.task_management.infrastructure.task.adapter.in.web;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.CreateTaskCommand;
import com.github.sansarch.task_management.application.task.dto.SortDirection;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.dto.TaskSortField;
import com.github.sansarch.task_management.application.task.dto.UpdateTaskCommand;
import com.github.sansarch.task_management.application.task.port.in.CompleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.CreateTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.DeleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.ListTasksUseCase;
import com.github.sansarch.task_management.application.task.port.in.MarkTaskInProgressUseCase;
import com.github.sansarch.task_management.application.task.port.in.UpdateTaskUseCase;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import com.github.sansarch.task_management.infrastructure.shared.web.response.PageResponse;
import com.github.sansarch.task_management.infrastructure.task.adapter.in.web.request.TaskRequest;
import com.github.sansarch.task_management.infrastructure.task.adapter.in.web.response.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Tasks", description = "Task management operations")
@RestController
@RequestMapping("/api/tasks")
@Validated
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

    @Operation(summary = "Create a task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
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

    @Operation(summary = "List tasks", description = "Returns a paginated list of tasks, optionally filtered by status and/or priority, sorted by the given field and direction")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved")
    @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
    @GetMapping
    public PageResponse<TaskResponse> list(@RequestParam(required = false) TaskStatus status,
                                           @RequestParam(required = false) TaskPriority priority,
                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                           @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
                                           @RequestParam(defaultValue = "CREATED_AT") TaskSortField sortBy,
                                           @RequestParam(defaultValue = "DESC") SortDirection sortDirection) {
        PageResult<TaskResult> result = listTasksUseCase.list(
                new TaskFilter(status, priority),
                new TaskPageRequest(page, size, sortBy, sortDirection)
        );
        return PageResponse.from(result, TaskResponse::from);
    }

    @Operation(summary = "Update a task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Task not found")
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

    @Operation(summary = "Delete a task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteTaskUseCase.delete(id);
    }

    @Operation(summary = "Mark a task as in progress", description = "Transitions the task from TODO to IN_PROGRESS")
    @ApiResponse(responseCode = "200", description = "Task marked as in progress")
    @ApiResponse(responseCode = "400", description = "Invalid state transition")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @PatchMapping("/{id}/start")
    public TaskResponse markInProgress(@PathVariable UUID id) {
        return TaskResponse.from(markTaskInProgressUseCase.markInProgress(id));
    }

    @Operation(summary = "Complete a task", description = "Transitions the task from TODO or IN_PROGRESS to DONE")
    @ApiResponse(responseCode = "200", description = "Task completed")
    @ApiResponse(responseCode = "400", description = "Invalid state transition")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable UUID id) {
        return TaskResponse.from(completeTaskUseCase.complete(id));
    }
}
