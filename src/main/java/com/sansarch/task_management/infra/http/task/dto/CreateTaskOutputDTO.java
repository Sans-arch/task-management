package com.sansarch.task_management.infra.http.task.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CreateTaskOutputDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean isCompleted;
}
