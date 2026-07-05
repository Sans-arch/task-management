package com.github.sansarch.task_management.application.shared.port.out;

import com.github.sansarch.task_management.domain.user.model.UserId;

public interface AuthenticatedUserProvider {
    UserId getCurrentUserId();
}
