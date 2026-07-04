package com.github.sansarch.task_management.infrastructure.user.adapter.out.persistence;

import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.model.User;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserGateway {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository, UserMapper userMapper) {
        this.springDataUserRepository = springDataUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataUserRepository.findById(id.id()).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = springDataUserRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }
}
