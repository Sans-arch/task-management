package com.github.sansarch.task_management.application.user.service;

import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.application.user.dto.UserResult;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.exception.UserNotFoundException;
import com.github.sansarch.task_management.domain.user.model.User;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCurrentUserService")
class GetCurrentUserServiceTest {

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private GetCurrentUserService getCurrentUserService;

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("should return the authenticated user's data")
        void shouldReturnAuthenticatedUserData() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(user.getId());
            when(userGateway.findById(user.getId())).thenReturn(Optional.of(user));

            UserResult result = getCurrentUserService.getCurrentUser();

            assertThat(result.email()).isEqualTo("jane@example.com");
            assertThat(result.displayName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when the authenticated user no longer exists")
        void shouldThrowWhenUserNoLongerExists() {
            UserId userId = UserId.generate();
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId);
            when(userGateway.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getCurrentUserService.getCurrentUser())
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
