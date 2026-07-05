package com.github.sansarch.task_management.infrastructure.auth.adapter.in.web.response;

import com.github.sansarch.task_management.application.auth.dto.LoginResult;

public record LoginResponse(String token, String tokenType, long expiresInSeconds) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.token(), "Bearer", result.expiresInSeconds());
    }
}
