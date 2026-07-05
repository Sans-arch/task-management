package com.github.sansarch.task_management.application.group.port.in;

import com.github.sansarch.task_management.application.group.dto.CreateGroupCommand;
import com.github.sansarch.task_management.application.group.dto.GroupResult;

public interface CreateGroupUseCase {
    GroupResult create(CreateGroupCommand command);
}
