package com.sansarch.task_management.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class Task {
    private Long id;
    private String title;
    private String description;
    private boolean isCompleted;
    private LocalDateTime dueDate;
}
