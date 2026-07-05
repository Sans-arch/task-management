package com.github.sansarch.task_management.application.group.service;

import com.github.sansarch.task_management.application.group.dto.GroupResult;
import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.application.shared.port.out.AuthenticatedUserProvider;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListMyGroupsService")
class ListMyGroupsServiceTest {

    private static final UserId CURRENT_USER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
    private static final UserId OTHER_MEMBER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000bb"));

    @Mock
    private GroupGateway groupGateway;

    @Mock
    private GroupMembershipGateway groupMembershipGateway;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private ListMyGroupsService listMyGroupsService;

    @Nested
    @DisplayName("listMine()")
    class ListMine {

        @Test
        @DisplayName("should return a GroupResult with member ids for each group the user belongs to")
        void shouldReturnGroupsWithMemberIds() {
            Group group = Group.create("Engineering");
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupGateway.findAllByMemberId(CURRENT_USER_ID)).thenReturn(List.of(group));
            when(groupMembershipGateway.findMemberIds(group.getId())).thenReturn(List.of(CURRENT_USER_ID, OTHER_MEMBER_ID));

            List<GroupResult> results = listMyGroupsService.listMine();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).name()).isEqualTo("Engineering");
            assertThat(results.get(0).memberIds()).containsExactlyInAnyOrder(CURRENT_USER_ID.id(), OTHER_MEMBER_ID.id());
        }

        @Test
        @DisplayName("should return an empty list when the user belongs to no groups")
        void shouldReturnEmptyListWhenNoGroups() {
            when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
            when(groupGateway.findAllByMemberId(CURRENT_USER_ID)).thenReturn(List.of());

            List<GroupResult> results = listMyGroupsService.listMine();

            assertThat(results).isEmpty();
        }
    }
}
