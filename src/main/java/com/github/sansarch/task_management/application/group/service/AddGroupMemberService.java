package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.AddGroupMemberCommand;
import com.github.sansarch.task_management.application.group.port.in.AddGroupMemberUseCase;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.group.exception.GroupAccessDeniedException;
import com.github.sansarch.task_management.domain.group.exception.GroupNotFoundException;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;
import com.github.sansarch.task_management.domain.user.exception.UserNotFoundException;
import com.github.sansarch.task_management.domain.user.model.User;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

@Service
public class AddGroupMemberService implements AddGroupMemberUseCase {

    private final GroupGateway groupGateway;
    private final GroupMembershipGateway groupMembershipGateway;
    private final UserGateway userGateway;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public AddGroupMemberService(GroupGateway groupGateway, GroupMembershipGateway groupMembershipGateway,
                                 UserGateway userGateway, AuthenticatedUserProvider authenticatedUserProvider) {
        this.groupGateway = groupGateway;
        this.groupMembershipGateway = groupMembershipGateway;
        this.userGateway = userGateway;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public void addMember(AddGroupMemberCommand command) {
        GroupId groupId = new GroupId(command.groupId());
        Group group = groupGateway.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + command.groupId()));

        UserId requesterId = authenticatedUserProvider.getCurrentUserId();
        if (!groupMembershipGateway.isMember(groupId, requesterId)) {
            throw new GroupAccessDeniedException("Only group members can add members to group " + group.getId().id());
        }

        User newMember = userGateway.findByEmail(command.memberEmail())
                .orElseThrow(() -> new UserNotFoundException("No user with email: " + command.memberEmail()));

        groupMembershipGateway.addMember(groupId, newMember.getId());
    }
}
