package com.github.sansarch.task_management.application.user.dto;

public record RegisterUserCommand(String email, String password, String displayName) {
}
