package com.github.sansarch.task_management.application.auth.port.out;

import com.github.sansarch.task_management.domain.user.model.UserId;

public interface TokenIssuer {
    String issue(UserId userId, String email);
    long expiresInSeconds();
}
