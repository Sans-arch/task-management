package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.CreateGroupCommand;
import com.github.sansarch.task_management.application.group.dto.GroupResult;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGroupService")
class CreateGroupServiceTest {

    private static final UserId CREATOR_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));

    @Mock
    private GroupGateway groupGateway;

    @Mock
    private GroupMembershipGateway groupMembershipGateway;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private CreateGroupService createGroupService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should return a GroupResult with the creator as the only member")
        void shouldReturnGroupResultWithCreatorAsMember() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CREATOR_ID);
            when(groupGateway.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

            GroupResult result = createGroupService.create(new CreateGroupCommand("Engineering"));

            assertThat(result.name()).isEqualTo("Engineering");
            assertThat(result.memberIds()).containsExactly(CREATOR_ID.id());
        }

        @Test
        @DisplayName("should add the creator as a group member")
        void shouldAddCreatorAsMember() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CREATOR_ID);
            when(groupGateway.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

            GroupResult result = createGroupService.create(new CreateGroupCommand("Engineering"));

            verify(groupMembershipGateway).addMember(new GroupId(result.id()), CREATOR_ID);
        }
    }
}
