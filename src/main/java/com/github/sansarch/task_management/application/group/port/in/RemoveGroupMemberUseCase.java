package com.github.sansarch.task_management.application.group.port.in;

import com.github.sansarch.task_management.application.group.dto.RemoveGroupMemberCommand;

public interface RemoveGroupMemberUseCase {
    void removeMember(RemoveGroupMemberCommand command);
}
