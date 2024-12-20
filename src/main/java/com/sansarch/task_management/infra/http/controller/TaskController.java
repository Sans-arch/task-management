package com.sansarch.task_management.infra.http.controller;

import com.sansarch.task_management.domain.entity.Task;
import com.sansarch.task_management.domain.service.TaskService;
import com.sansarch.task_management.infra.http.dto.CreateTaskInputDTO;
import com.sansarch.task_management.infra.http.dto.CreateTaskOutputDTO;
import com.sansarch.task_management.infra.http.dto.UpdateTaskInputDTO;
import com.sansarch.task_management.infra.http.dto.UpdateTaskOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping(value = "/list")
    public List<Task> listTasks() {
        return taskService.listTasks();
    }

    @GetMapping(value = "/{id}")
    public Task retrieveTask(@PathVariable Long id) {
        return taskService.retrieveTask(id);
    }

    @PostMapping
    public CreateTaskOutputDTO createTask(@RequestBody CreateTaskInputDTO input) {
        return taskService.createTask(input);
    }

    @PutMapping(value = "/{id}")
    public UpdateTaskOutputDTO updateTask(@PathVariable Long id, @RequestBody UpdateTaskInputDTO input) {
        return taskService.updateTask(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
