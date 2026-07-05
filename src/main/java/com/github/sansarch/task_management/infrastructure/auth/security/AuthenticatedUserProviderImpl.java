package com.github.sansarch.task_management.infrastructure.auth.security;

import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProviderImpl implements AuthenticatedUserProvider {

    @Override
    public UserId getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserId userId)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return userId;
    }
}
