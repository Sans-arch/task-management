package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.AddGroupMemberCommand;
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
@DisplayName("AddGroupMemberService")
class AddGroupMemberServiceTest {

    private static final UserId REQUESTER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));

    @Mock
    private GroupGateway groupGateway;

    @Mock
    private GroupMembershipGateway groupMembershipGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private AddGroupMemberService addGroupMemberService;

    @Nested
    @DisplayName("addMember()")
    class AddMember {

        @Test
        @DisplayName("should add the member when the requester is already a group member")
        void shouldAddMemberWhenRequesterIsMember() {
            Group group = Group.create("Engineering");
            User newMember = User.create("jane@example.com", "hash", "Jane");
            when(groupGateway.findById(group.getId())).thenReturn(Optional.of(group));
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(REQUESTER_ID);
            when(groupMembershipGateway.isMember(group.getId(), REQUESTER_ID)).thenReturn(true);
            when(userGateway.findByEmail("jane@example.com")).thenReturn(Optional.of(newMember));

            addGroupMemberService.addMember(new AddGroupMemberCommand(group.getId().id(), "jane@example.com"));

            verify(groupMembershipGateway).addMember(group.getId(), newMember.getId());
        }

        @Test
        @DisplayName("should throw GroupNotFoundException when the group does not exist")
        void shouldThrowWhenGroupNotFound() {
            GroupId groupId = GroupId.generate();
            when(groupGateway.findById(groupId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addGroupMemberService.addMember(new AddGroupMemberCommand(groupId.id(), "jane@example.com")))
                    .isInstanceOf(GroupNotFoundException.class);
        }

        @Test
        @DisplayName("should throw GroupAccessDeniedException when the requester is not a member")
        void shouldThrowWhenRequesterNotMember() {
            Group group = Group.create("Engineering");
            when(groupGateway.findById(group.getId())).thenReturn(Optional.of(group));
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(REQUESTER_ID);
            when(groupMembershipGateway.isMember(group.getId(), REQUESTER_ID)).thenReturn(false);

            assertThatThrownBy(() -> addGroupMemberService.addMember(new AddGroupMemberCommand(group.getId().id(), "jane@example.com")))
                    .isInstanceOf(GroupAccessDeniedException.class);

            verify(groupMembershipGateway, never()).addMember(any(), any());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when no user has the given email")
        void shouldThrowWhenUserNotFound() {
            Group group = Group.create("Engineering");
            when(groupGateway.findById(group.getId())).thenReturn(Optional.of(group));
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(REQUESTER_ID);
            when(groupMembershipGateway.isMember(group.getId(), REQUESTER_ID)).thenReturn(true);
            when(userGateway.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addGroupMemberService.addMember(new AddGroupMemberCommand(group.getId().id(), "unknown@example.com")))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
