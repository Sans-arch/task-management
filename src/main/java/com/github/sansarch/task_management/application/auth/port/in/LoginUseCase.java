package com.github.sansarch.task_management.application.auth.port.in;

import com.github.sansarch.task_management.application.auth.dto.LoginCommand;
import com.github.sansarch.task_management.application.auth.dto.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
