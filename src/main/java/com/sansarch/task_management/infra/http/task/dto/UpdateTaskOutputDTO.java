package com.sansarch.task_management.infra.http.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UpdateTaskOutputDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;

    @JsonProperty("isCompleted")
    private boolean isCompleted;
}
