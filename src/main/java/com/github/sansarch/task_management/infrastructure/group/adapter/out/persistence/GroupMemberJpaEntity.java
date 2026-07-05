package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_members")
public class GroupMemberJpaEntity {

    @EmbeddedId
    private GroupMemberId id;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    protected GroupMemberJpaEntity() {
    }

    public GroupMemberJpaEntity(UUID groupId, UUID userId, LocalDateTime joinedAt) {
        this.id = new GroupMemberId(groupId, userId);
        this.joinedAt = joinedAt;
    }

    public GroupMemberId getId() { return id; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
