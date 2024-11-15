package com.sansarch.task_management.infra.repository;

import com.sansarch.task_management.infra.repository.model.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskModel, Long> {
}
