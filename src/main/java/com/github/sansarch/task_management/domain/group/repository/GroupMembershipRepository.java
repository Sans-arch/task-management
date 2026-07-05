package com.github.sansarch.task_management.domain.group.repository;

import com.github.sansarch.task_management.domain.group.model.GroupId;
import com.github.sansarch.task_management.domain.user.model.UserId;

import java.util.List;

public interface GroupMembershipRepository {
    void addMember(GroupId groupId, UserId userId);
    void removeMember(GroupId groupId, UserId userId);
    boolean isMember(GroupId groupId, UserId userId);
    List<UserId> findMemberIds(GroupId groupId);
}
