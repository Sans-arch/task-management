package com.github.sansarch.task_management.application.task.dto;

public enum TaskSortField {
    TITLE("title"),
    STATUS("status"),
    PRIORITY("priority"),
    DUE_DATE("dueDate"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String fieldName;

    TaskSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}
