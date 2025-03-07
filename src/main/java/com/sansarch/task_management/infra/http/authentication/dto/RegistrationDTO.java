package com.sansarch.task_management.infra.http.authentication.dto;

import com.sansarch.task_management.domain.user.entity.UserRole;

public record RegistrationDTO(String login, String password, UserRole role) {
}
