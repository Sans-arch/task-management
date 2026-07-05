package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SpringDataGroupMemberRepository extends JpaRepository<GroupMemberJpaEntity, GroupMemberId> {

    @Query("SELECT gm.id.userId FROM GroupMemberJpaEntity gm WHERE gm.id.groupId = :groupId")
    List<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);

    @Query("SELECT DISTINCT gm2.id.userId FROM GroupMemberJpaEntity gm1 " +
           "JOIN GroupMemberJpaEntity gm2 ON gm1.id.groupId = gm2.id.groupId " +
           "WHERE gm1.id.userId = :userId AND gm2.id.userId <> :userId")
    Set<UUID> findCoMemberIds(@Param("userId") UUID userId);
}
