package com.github.sansarch.task_management.application.group.port.out;

import com.github.sansarch.task_management.domain.group.repository.GroupMembershipRepository;
import com.github.sansarch.task_management.domain.user.model.UserId;

import java.util.Set;

public interface GroupMembershipGateway extends GroupMembershipRepository {
    Set<UserId> findCoMemberIds(UserId userId);
}
