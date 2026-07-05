package com.github.sansarch.task_management.application.user.service;

import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.application.user.dto.UserResult;
import com.github.sansarch.task_management.application.user.port.in.GetCurrentUserUseCase;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.exception.UserNotFoundException;
import com.github.sansarch.task_management.domain.user.model.User;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserGateway userGateway;

    public GetCurrentUserService(AuthenticatedUserProvider authenticatedUserProvider, UserGateway userGateway) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userGateway = userGateway;
    }

    @Override
    public UserResult getCurrentUser() {
        UserId id = authenticatedUserProvider.getCurrentUserId();
        User user = userGateway.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id.id()));

        return new UserResult(
                user.getId().id(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
