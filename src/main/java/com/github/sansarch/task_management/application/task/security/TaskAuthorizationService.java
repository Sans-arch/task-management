package com.github.sansarch.task_management.application.task.security;

import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.task.exception.TaskAccessDeniedException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TaskAuthorizationService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final GroupMembershipGateway groupMembershipGateway;

    public TaskAuthorizationService(AuthenticatedUserProvider authenticatedUserProvider,
                                    GroupMembershipGateway groupMembershipGateway) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.groupMembershipGateway = groupMembershipGateway;
    }

    public Set<UserId> manageableOwnerIds() {
        UserId currentUserId = authenticatedUserProvider.getCurrentUserId();
        Set<UserId> manageableOwnerIds = new HashSet<>(groupMembershipGateway.findCoMemberIds(currentUserId));
        manageableOwnerIds.add(currentUserId);
        return manageableOwnerIds;
    }

    public void assertCanManage(Task task) {
        if (!manageableOwnerIds().contains(task.getOwnerId())) {
            throw new TaskAccessDeniedException("User cannot manage task " + task.getId().id());
        }
    }
}
