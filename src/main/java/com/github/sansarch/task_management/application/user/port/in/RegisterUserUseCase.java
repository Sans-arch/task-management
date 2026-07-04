package com.github.sansarch.task_management.application.user.port.in;

import com.github.sansarch.task_management.application.user.dto.RegisterUserCommand;
import com.github.sansarch.task_management.application.user.dto.UserResult;

public interface RegisterUserUseCase {
    UserResult register(RegisterUserCommand command);
}
