package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.CreateGroupCommand;
import com.github.sansarch.task_management.application.group.dto.GroupResult;
import com.github.sansarch.task_management.application.group.port.in.CreateGroupUseCase;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateGroupService implements CreateGroupUseCase {

    private final GroupGateway groupGateway;
    private final GroupMembershipGateway groupMembershipGateway;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CreateGroupService(GroupGateway groupGateway, GroupMembershipGateway groupMembershipGateway,
                              AuthenticatedUserProvider authenticatedUserProvider) {
        this.groupGateway = groupGateway;
        this.groupMembershipGateway = groupMembershipGateway;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public GroupResult create(CreateGroupCommand command) {
        UserId creatorId = authenticatedUserProvider.getCurrentUserId();

        Group saved = groupGateway.save(Group.create(command.name()));
        groupMembershipGateway.addMember(saved.getId(), creatorId);

        return new GroupResult(saved.getId().id(), saved.getName(), saved.getCreatedAt(), List.of(creatorId.id()));
    }
}
