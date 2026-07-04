package com.github.sansarch.task_management.application.user.service;

import com.github.sansarch.task_management.application.user.dto.RegisterUserCommand;
import com.github.sansarch.task_management.application.user.dto.UserResult;
import com.github.sansarch.task_management.application.user.port.in.RegisterUserUseCase;
import com.github.sansarch.task_management.application.user.port.out.PasswordHasher;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.exception.DuplicateEmailException;
import com.github.sansarch.task_management.domain.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserGateway userGateway;
    private final PasswordHasher passwordHasher;

    public RegisterUserService(UserGateway userGateway, PasswordHasher passwordHasher) {
        this.userGateway = userGateway;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserResult register(RegisterUserCommand command) {
        userGateway.findByEmail(command.email()).ifPresent(existing -> {
            throw new DuplicateEmailException("Email already registered: " + command.email());
        });

        String passwordHash = passwordHasher.hash(command.password());
        User user = User.create(command.email(), passwordHash, command.displayName());
        User saved = userGateway.save(user);

        return toResult(saved);
    }

    private UserResult toResult(User user) {
        return new UserResult(
                user.getId().id(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
