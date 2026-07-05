package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import com.github.sansarch.task_management.application.group.port.out.GroupMembershipGateway;
import com.github.sansarch.task_management.domain.group.model.GroupId;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GroupMembershipRepositoryAdapter implements GroupMembershipGateway {

    private final SpringDataGroupMemberRepository springDataGroupMemberRepository;

    public GroupMembershipRepositoryAdapter(SpringDataGroupMemberRepository springDataGroupMemberRepository) {
        this.springDataGroupMemberRepository = springDataGroupMemberRepository;
    }

    @Override
    public void addMember(GroupId groupId, UserId userId) {
        GroupMemberId id = new GroupMemberId(groupId.id(), userId.id());
        if (springDataGroupMemberRepository.existsById(id)) {
            return;
        }
        springDataGroupMemberRepository.save(new GroupMemberJpaEntity(groupId.id(), userId.id(), LocalDateTime.now()));
    }

    @Override
    public void removeMember(GroupId groupId, UserId userId) {
        springDataGroupMemberRepository.deleteById(new GroupMemberId(groupId.id(), userId.id()));
    }

    @Override
    public boolean isMember(GroupId groupId, UserId userId) {
        return springDataGroupMemberRepository.existsById(new GroupMemberId(groupId.id(), userId.id()));
    }

    @Override
    public List<UserId> findMemberIds(GroupId groupId) {
        return springDataGroupMemberRepository.findUserIdsByGroupId(groupId.id()).stream()
                .map(UserId::new)
                .toList();
    }

    @Override
    public Set<UserId> findCoMemberIds(UserId userId) {
        return springDataGroupMemberRepository.findCoMemberIds(userId.id()).stream()
                .map(UserId::new)
                .collect(Collectors.toSet());
    }
}
