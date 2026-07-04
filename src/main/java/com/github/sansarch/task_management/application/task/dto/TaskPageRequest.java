package com.github.sansarch.task_management.application.task.dto;

public record TaskPageRequest(int page, int size, TaskSortField sortBy, SortDirection sortDirection) {
}
