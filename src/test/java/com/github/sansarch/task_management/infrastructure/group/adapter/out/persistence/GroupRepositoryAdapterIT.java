package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import({GroupRepositoryAdapter.class, GroupMapper.class, GroupMembershipRepositoryAdapter.class})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("GroupRepositoryAdapter")
class GroupRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private GroupRepositoryAdapter groupRepositoryAdapter;

    @Autowired
    private GroupMembershipRepositoryAdapter groupMembershipRepositoryAdapter;

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist and return the group with all fields")
        void shouldPersistAndReturnGroup() {
            Group group = Group.create("Engineering");

            Group saved = groupRepositoryAdapter.save(group);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Engineering");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return present Optional when group exists")
        void shouldReturnGroupWhenFound() {
            Group group = Group.create("Engineering");
            groupRepositoryAdapter.save(group);

            Optional<Group> found = groupRepositoryAdapter.findById(group.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("should return empty Optional when group does not exist")
        void shouldReturnEmptyWhenNotFound() {
            Optional<Group> found = groupRepositoryAdapter.findById(GroupId.generate());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByMemberId()")
    class FindAllByMemberId {

        @Test
        @DisplayName("should return only groups the user is a member of")
        void shouldReturnOnlyGroupsUserBelongsTo() {
            UserId userId = UserId.generate();
            UserId otherUserId = UserId.generate();
            Group myGroup = groupRepositoryAdapter.save(Group.create("My Group"));
            Group otherGroup = groupRepositoryAdapter.save(Group.create("Other Group"));
            groupMembershipRepositoryAdapter.addMember(myGroup.getId(), userId);
            groupMembershipRepositoryAdapter.addMember(otherGroup.getId(), otherUserId);

            List<Group> result = groupRepositoryAdapter.findAllByMemberId(userId);

            assertThat(result).extracting(Group::getName).containsExactly("My Group");
        }

        @Test
        @DisplayName("should return an empty list when the user belongs to no groups")
        void shouldReturnEmptyListWhenNoMemberships() {
            List<Group> result = groupRepositoryAdapter.findAllByMemberId(UserId.generate());

            assertThat(result).isEmpty();
        }
    }
}
