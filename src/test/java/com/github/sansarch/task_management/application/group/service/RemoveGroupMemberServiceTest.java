package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.RemoveGroupMemberCommand;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.group.exception.GroupAccessDeniedException;
import com.github.sansarch.task_management.domain.group.exception.GroupNotFoundException;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoveGroupMemberService")
class RemoveGroupMemberServiceTest {

    private static final UserId REQUESTER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
    private static final UserId TARGET_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000bb"));

    @Mock
    private GroupGateway groupGateway;

    @Mock
    private GroupMembershipGateway groupMembershipGateway;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private RemoveGroupMemberService removeGroupMemberService;

    @Nested
    @DisplayName("removeMember()")
    class RemoveMember {

        @Test
        @DisplayName("should remove another member when the requester is a group member")
        void shouldRemoveAnotherMember() {
            Group group = Group.create("Engineering");
            when(groupGateway.findById(group.getId())).thenReturn(Optional.of(group));
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(REQUESTER_ID);
            when(groupMembershipGateway.isMember(group.getId(), REQUESTER_ID)).thenReturn(true);

            removeGroupMemberService.removeMember(new RemoveGroupMemberCommand(group.getId().id(), TARGET_ID.id()));

            verify(groupMembershipGateway).removeMember(group.getId(), TARGET_ID);
        }

        @Test
        @DisplayName("should allow a member to remove themself (leave the group)")
        void shouldAllowSelfRemoval() {
            Group group = Group.create("Engineering");
            when(groupGateway.findById(group.getId())).thenReturn(Optional.of(group));
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(REQUESTER_ID);
            when(groupMembershipGateway.isMember(group.getId(), REQUESTER_ID)).thenReturn(true);

            removeGroupMemberService.removeMember(new RemoveGroupMemberCommand(group.getId().id(), REQUESTER_ID.id()));

            verify(groupMembershipGateway).removeMember(group.getId(), REQUESTER_ID);
        }

        @Test
        @DisplayName("should throw GroupNotFoundException when the group does not exist")
        void shouldThrowWhenGroupNotFound() {
            GroupId groupId = GroupId.generate();
            when(groupGateway.findById(groupId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> removeGroupMemberService.removeMember(new RemoveGroupMemberCommand(groupId.id(), TARGET_ID.id())))
                    .isInstanceOf(GroupNotFoundException.class);
        }

        @Test
        @DisplayName("should throw GroupAccessDeniedException when the requester is not a member")
        void shouldThrowWhenRequesterNotMember() {
            Group group = Group.create("Engineering");
            when(groupGateway.findById(group.getId())).thenReturn(Optional.of(group));
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(REQUESTER_ID);
            when(groupMembershipGateway.isMember(group.getId(), REQUESTER_ID)).thenReturn(false);

            assertThatThrownBy(() -> removeGroupMemberService.removeMember(new RemoveGroupMemberCommand(group.getId().id(), TARGET_ID.id())))
                    .isInstanceOf(GroupAccessDeniedException.class);

            verify(groupMembershipGateway, never()).removeMember(any(), any());
        }
    }
}
