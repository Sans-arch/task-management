package com.github.sansarch.task_management.infrastructure.task.adapter.out.persistence;

import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataTaskRepository extends JpaRepository<TaskJpaEntity, UUID> {

    @Query("SELECT t FROM TaskJpaEntity t WHERE (:status IS NULL OR t.status = :status) AND (:priority IS NULL OR t.priority = :priority)")
    Page<TaskJpaEntity> findByFilter(@Param("status") TaskStatus status, @Param("priority") TaskPriority priority, Pageable pageable);
}
