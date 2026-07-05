package com.github.sansarch.task_management.domain.group.repository;

import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;

import java.util.Optional;

public interface GroupRepository {
    Optional<Group> findById(GroupId id);
    Group save(Group group);
}
