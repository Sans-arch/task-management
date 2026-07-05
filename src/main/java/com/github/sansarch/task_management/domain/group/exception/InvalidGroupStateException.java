package com.github.sansarch.task_management.domain.group.exception;

public class InvalidGroupStateException extends RuntimeException {
    public InvalidGroupStateException(String message) {
        super(message);
    }
}
