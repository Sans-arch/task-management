package com.github.sansarch.task_management.application.task.security;

import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.task.exception.TaskAccessDeniedException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TaskAuthorizationService {

    private final AuthenticatedUserProvider authenticatedUserProvider;

    public TaskAuthorizationService(AuthenticatedUserProvider authenticatedUserProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    // TODO(Milestone 4): grow this to include the current user's group co-members.
    public Set<UserId> manageableOwnerIds() {
        return Set.of(authenticatedUserProvider.getCurrentUserId());
    }

    public void assertCanManage(Task task) {
        if (!manageableOwnerIds().contains(task.getOwnerId())) {
            throw new TaskAccessDeniedException("User cannot manage task " + task.getId().id());
        }
    }
}
