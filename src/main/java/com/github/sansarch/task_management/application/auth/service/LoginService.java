package com.github.sansarch.task_management.application.auth.service;

import com.github.sansarch.task_management.application.auth.dto.LoginCommand;
import com.github.sansarch.task_management.application.auth.dto.LoginResult;
import com.github.sansarch.task_management.application.auth.port.in.LoginUseCase;
import com.github.sansarch.task_management.application.auth.port.out.TokenIssuer;
import com.github.sansarch.task_management.application.user.port.out.PasswordHasher;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.exception.InvalidCredentialsException;
import com.github.sansarch.task_management.domain.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserGateway userGateway;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginService(UserGateway userGateway, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userGateway = userGateway;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userGateway.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        String token = tokenIssuer.issue(user.getId(), user.getEmail());
        return new LoginResult(token, user.getId().id(), user.getEmail(), tokenIssuer.expiresInSeconds());
    }
}
