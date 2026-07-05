package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.RemoveGroupMemberCommand;
import com.github.sansarch.task_management.application.group.port.in.RemoveGroupMemberUseCase;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.group.exception.GroupAccessDeniedException;
import com.github.sansarch.task_management.domain.group.exception.GroupNotFoundException;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

@Service
public class RemoveGroupMemberService implements RemoveGroupMemberUseCase {

    private final GroupGateway groupGateway;
    private final GroupMembershipGateway groupMembershipGateway;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public RemoveGroupMemberService(GroupGateway groupGateway, GroupMembershipGateway groupMembershipGateway,
                                    AuthenticatedUserProvider authenticatedUserProvider) {
        this.groupGateway = groupGateway;
        this.groupMembershipGateway = groupMembershipGateway;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public void removeMember(RemoveGroupMemberCommand command) {
        GroupId groupId = new GroupId(command.groupId());
        Group group = groupGateway.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + command.groupId()));

        UserId requesterId = authenticatedUserProvider.getCurrentUserId();
        if (!groupMembershipGateway.isMember(groupId, requesterId)) {
            throw new GroupAccessDeniedException("Only group members can remove members from group " + group.getId().id());
        }

        groupMembershipGateway.removeMember(groupId, new UserId(command.userId()));
    }
}
