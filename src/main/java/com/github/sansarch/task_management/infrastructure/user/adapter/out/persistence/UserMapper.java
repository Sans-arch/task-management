package com.github.sansarch.task_management.infrastructure.user.adapter.out.persistence;

import com.github.sansarch.task_management.domain.user.model.User;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId().id(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                new UserId(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
