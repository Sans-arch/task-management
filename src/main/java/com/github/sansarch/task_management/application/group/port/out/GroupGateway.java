package com.github.sansarch.task_management.application.group.port.out;

import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.repository.GroupRepository;
import com.github.sansarch.task_management.domain.user.model.UserId;

import java.util.List;

public interface GroupGateway extends GroupRepository {
    List<Group> findAllByMemberId(UserId userId);
}
