package com.sansarch.task_management.infra.http.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpdateTaskOutputDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean isCompleted;
}
