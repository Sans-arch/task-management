package com.sansarch.task_management.service;

import com.sansarch.task_management.entity.Task;
import com.sansarch.task_management.exception.TaskNotFoundException;
import com.sansarch.task_management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public List<Task> listTasks() {
        return taskRepository.findAll();
    }

    public Object createTask() {
        return "Task created!";
    }

    public Task retrieveTask(Long id) {
        var task = taskRepository.findById(id);

        if (task.isEmpty()) {
            throw new TaskNotFoundException("Task not found!");
        }

        return task.get();
    }

    public String updateTask() {
        return "Task updated!";
    }

    public String deleteTask() {
        return "Task deleted!";
    }
}
