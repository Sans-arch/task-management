package com.github.sansarch.task_management.application.group.port.in;

import com.github.sansarch.task_management.application.group.dto.AddGroupMemberCommand;

public interface AddGroupMemberUseCase {
    void addMember(AddGroupMemberCommand command);
}
