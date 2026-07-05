package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.GroupResult;
import com.github.sansarch.task_management.application.group.port.in.ListMyGroupsUseCase;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListMyGroupsService implements ListMyGroupsUseCase {

    private final GroupGateway groupGateway;
    private final GroupMembershipGateway groupMembershipGateway;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ListMyGroupsService(GroupGateway groupGateway, GroupMembershipGateway groupMembershipGateway,
                               AuthenticatedUserProvider authenticatedUserProvider) {
        this.groupGateway = groupGateway;
        this.groupMembershipGateway = groupMembershipGateway;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public List<GroupResult> listMine() {
        UserId currentUserId = authenticatedUserProvider.getCurrentUserId();

        return groupGateway.findAllByMemberId(currentUserId).stream()
                .map(this::toResult)
                .toList();
    }

    private GroupResult toResult(Group group) {
        List<UUID> memberIds = groupMembershipGateway.findMemberIds(group.getId()).stream()
                .map(UserId::id)
                .toList();
        return new GroupResult(group.getId().id(), group.getName(), group.getCreatedAt(), memberIds);
    }
}
