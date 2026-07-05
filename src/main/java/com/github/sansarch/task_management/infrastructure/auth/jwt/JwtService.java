package com.github.sansarch.task_management.infrastructure.auth.jwt;

import com.github.sansarch.task_management.domain.user.model.UserId;
import com.github.sansarch.task_management.infrastructure.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService {

    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserId userId, String email) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId.id().toString())
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<UserId> parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UserId(UUID.fromString(claims.getSubject())));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
