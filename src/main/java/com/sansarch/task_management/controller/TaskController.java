package com.sansarch.task_management.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String listTasks() {
        return "Hello World!";
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String createTask() {
        return "Task created!";
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String retrieveTask() {
        return "Task retrieved!";
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateTask() {
        return "Task updated!";
    }

    @DeleteMapping("/{id}")
    public String deleteTask() {
        return "Task deleted!";
    }

}
