package com.sansarch.task_management.infra.http.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpdateTaskInputDTO {
    private String title;
    private String description;
    private boolean isCompleted;
    private LocalDateTime dueDate;
}
