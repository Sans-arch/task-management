package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import com.github.sansarch.task_management.application.group.port.out.GroupGateway;
import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GroupRepositoryAdapter implements GroupGateway {

    private final SpringDataGroupRepository springDataGroupRepository;
    private final GroupMapper groupMapper;

    public GroupRepositoryAdapter(SpringDataGroupRepository springDataGroupRepository, GroupMapper groupMapper) {
        this.springDataGroupRepository = springDataGroupRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return springDataGroupRepository.findById(id.id()).map(groupMapper::toDomain);
    }

    @Override
    public Group save(Group group) {
        GroupJpaEntity saved = springDataGroupRepository.save(groupMapper.toEntity(group));
        return groupMapper.toDomain(saved);
    }

    @Override
    public List<Group> findAllByMemberId(UserId userId) {
        return springDataGroupRepository.findAllByMemberId(userId.id()).stream()
                .map(groupMapper::toDomain)
                .toList();
    }
}
