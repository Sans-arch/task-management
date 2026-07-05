package com.github.sansarch.task_management.infrastructure.auth.adapter.out;

import com.github.sansarch.task_management.application.auth.port.out.TokenIssuer;
import com.github.sansarch.task_management.domain.user.model.UserId;
import com.github.sansarch.task_management.infrastructure.auth.config.JwtProperties;
import com.github.sansarch.task_management.infrastructure.auth.jwt.JwtService;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuerAdapter implements TokenIssuer {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public TokenIssuerAdapter(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String issue(UserId userId, String email) {
        return jwtService.generateToken(userId, email);
    }

    @Override
    public long expiresInSeconds() {
        return jwtProperties.expirationMinutes() * 60;
    }
}
