package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataGroupRepository extends JpaRepository<GroupJpaEntity, UUID> {

    @Query("SELECT g FROM GroupJpaEntity g JOIN GroupMemberJpaEntity gm ON gm.id.groupId = g.id " +
           "WHERE gm.id.userId = :userId")
    List<GroupJpaEntity> findAllByMemberId(@Param("userId") UUID userId);
}
