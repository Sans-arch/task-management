package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import({GroupMembershipRepositoryAdapter.class, GroupRepositoryAdapter.class, GroupMapper.class})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("GroupMembershipRepositoryAdapter")
class GroupMembershipRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private GroupMembershipRepositoryAdapter groupMembershipRepositoryAdapter;

    @Autowired
    private GroupRepositoryAdapter groupRepositoryAdapter;

    @Nested
    @DisplayName("addMember() / isMember()")
    class AddMemberAndIsMember {

        @Test
        @DisplayName("should make the user a member of the group")
        void shouldAddMember() {
            Group group = groupRepositoryAdapter.save(Group.create("Engineering"));
            UserId userId = UserId.generate();

            groupMembershipRepositoryAdapter.addMember(group.getId(), userId);

            assertThat(groupMembershipRepositoryAdapter.isMember(group.getId(), userId)).isTrue();
        }

        @Test
        @DisplayName("should be idempotent when adding the same member twice")
        void shouldBeIdempotent() {
            Group group = groupRepositoryAdapter.save(Group.create("Engineering"));
            UserId userId = UserId.generate();

            groupMembershipRepositoryAdapter.addMember(group.getId(), userId);
            groupMembershipRepositoryAdapter.addMember(group.getId(), userId);

            assertThat(groupMembershipRepositoryAdapter.findMemberIds(group.getId())).containsExactly(userId);
        }

        @Test
        @DisplayName("should return false for a user who was never added")
        void shouldReturnFalseForNonMember() {
            Group group = groupRepositoryAdapter.save(Group.create("Engineering"));

            assertThat(groupMembershipRepositoryAdapter.isMember(group.getId(), UserId.generate())).isFalse();
        }
    }

    @Nested
    @DisplayName("removeMember()")
    class RemoveMember {

        @Test
        @DisplayName("should remove the membership so the user is no longer a member")
        void shouldRemoveMember() {
            Group group = groupRepositoryAdapter.save(Group.create("Engineering"));
            UserId userId = UserId.generate();
            groupMembershipRepositoryAdapter.addMember(group.getId(), userId);

            groupMembershipRepositoryAdapter.removeMember(group.getId(), userId);

            assertThat(groupMembershipRepositoryAdapter.isMember(group.getId(), userId)).isFalse();
        }

        @Test
        @DisplayName("should be a no-op when removing a non-member")
        void shouldBeNoOpForNonMember() {
            Group group = groupRepositoryAdapter.save(Group.create("Engineering"));

            groupMembershipRepositoryAdapter.removeMember(group.getId(), UserId.generate());
        }
    }

    @Nested
    @DisplayName("findMemberIds()")
    class FindMemberIds {

        @Test
        @DisplayName("should return all member ids of the group")
        void shouldReturnAllMemberIds() {
            Group group = groupRepositoryAdapter.save(Group.create("Engineering"));
            UserId userA = UserId.generate();
            UserId userB = UserId.generate();
            groupMembershipRepositoryAdapter.addMember(group.getId(), userA);
            groupMembershipRepositoryAdapter.addMember(group.getId(), userB);

            List<UserId> memberIds = groupMembershipRepositoryAdapter.findMemberIds(group.getId());

            assertThat(memberIds).containsExactlyInAnyOrder(userA, userB);
        }
    }

    @Nested
    @DisplayName("findCoMemberIds()")
    class FindCoMemberIds {

        @Test
        @DisplayName("should return the other members of every group the user shares, excluding themself")
        void shouldReturnCoMembersAcrossSharedGroups() {
            UserId userA = UserId.generate();
            UserId userB = UserId.generate();
            UserId isolatedUser = UserId.generate();
            UserId thirdGroupUser = UserId.generate();

            Group sharedGroup = groupRepositoryAdapter.save(Group.create("Shared"));
            groupMembershipRepositoryAdapter.addMember(sharedGroup.getId(), userA);
            groupMembershipRepositoryAdapter.addMember(sharedGroup.getId(), userB);

            Group largerGroup = groupRepositoryAdapter.save(Group.create("Larger"));
            groupMembershipRepositoryAdapter.addMember(largerGroup.getId(), userA);
            groupMembershipRepositoryAdapter.addMember(largerGroup.getId(), thirdGroupUser);

            Group isolatedGroup = groupRepositoryAdapter.save(Group.create("Isolated"));
            groupMembershipRepositoryAdapter.addMember(isolatedGroup.getId(), isolatedUser);

            Set<UserId> coMembersOfA = groupMembershipRepositoryAdapter.findCoMemberIds(userA);
            Set<UserId> coMembersOfIsolated = groupMembershipRepositoryAdapter.findCoMemberIds(isolatedUser);

            assertThat(coMembersOfA).containsExactlyInAnyOrder(userB, thirdGroupUser);
            assertThat(coMembersOfIsolated).isEmpty();
        }
    }
}
