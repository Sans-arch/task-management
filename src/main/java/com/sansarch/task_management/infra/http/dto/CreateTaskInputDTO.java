package com.sansarch.task_management.infra.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateTaskInputDTO {
    private String title;
    private String description;
    private LocalDateTime dueDate;
}
