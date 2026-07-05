package com.github.sansarch.task_management.application.task.security;

import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.task.exception.TaskAccessDeniedException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskAuthorizationService")
class TaskAuthorizationServiceTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);
    private static final UserId CURRENT_USER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
    private static final UserId OTHER_USER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000bb"));
    private static final UserId CO_MEMBER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000cc"));

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private GroupMembershipGateway groupMembershipGateway;

    @InjectMocks
    private TaskAuthorizationService taskAuthorizationService;

    @Nested
    @DisplayName("manageableOwnerIds()")
    class ManageableOwnerIds {

        @Test
        @DisplayName("should return a set containing only the current user when there are no co-members")
        void shouldReturnOnlyCurrentUserWhenNoCoMembers() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupMembershipGateway.findCoMemberIds(CURRENT_USER_ID)).thenReturn(Set.of());

            Set<UserId> result = taskAuthorizationService.manageableOwnerIds();

            assertThat(result).containsExactly(CURRENT_USER_ID);
        }

        @Test
        @DisplayName("should include the current user's group co-members")
        void shouldIncludeCoMembers() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupMembershipGateway.findCoMemberIds(CURRENT_USER_ID)).thenReturn(Set.of(CO_MEMBER_ID));

            Set<UserId> result = taskAuthorizationService.manageableOwnerIds();

            assertThat(result).containsExactlyInAnyOrder(CURRENT_USER_ID, CO_MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("assertCanManage()")
    class AssertCanManage {

        @Test
        @DisplayName("should not throw when the task is owned by the current user")
        void shouldNotThrowWhenOwnedByCurrentUser() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupMembershipGateway.findCoMemberIds(CURRENT_USER_ID)).thenReturn(Set.of());
            Task task = Task.reconstitute(TaskId.generate(), CURRENT_USER_ID, "Title", null,
                    TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);

            taskAuthorizationService.assertCanManage(task);
        }

        @Test
        @DisplayName("should not throw when the task is owned by a group co-member")
        void shouldNotThrowWhenOwnedByCoMember() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupMembershipGateway.findCoMemberIds(CURRENT_USER_ID)).thenReturn(Set.of(CO_MEMBER_ID));
            Task task = Task.reconstitute(TaskId.generate(), CO_MEMBER_ID, "Title", null,
                    TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);

            taskAuthorizationService.assertCanManage(task);
        }

        @Test
        @DisplayName("should throw TaskAccessDeniedException when the task is owned by an unrelated user")
        void shouldThrowWhenOwnedByUnrelatedUser() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupMembershipGateway.findCoMemberIds(CURRENT_USER_ID)).thenReturn(Set.of(CO_MEMBER_ID));
            Task task = Task.reconstitute(TaskId.generate(), OTHER_USER_ID, "Title", null,
                    TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);

            assertThatThrownBy(() -> taskAuthorizationService.assertCanManage(task))
                    .isInstanceOf(TaskAccessDeniedException.class);
        }
    }
}
