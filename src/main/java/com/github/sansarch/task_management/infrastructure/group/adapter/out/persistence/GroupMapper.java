package com.github.sansarch.task_management.infrastructure.group.adapter.out.persistence;

import com.github.sansarch.task_management.domain.group.model.Group;
import com.github.sansarch.task_management.domain.group.model.GroupId;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupJpaEntity toEntity(Group group) {
        return new GroupJpaEntity(group.getId().id(), group.getName(), group.getCreatedAt());
    }

    public Group toDomain(GroupJpaEntity entity) {
        return Group.reconstitute(new GroupId(entity.getId()), entity.getName(), entity.getCreatedAt());
    }
}
