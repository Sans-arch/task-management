package com.github.sansarch.task_management.infrastructure.auth.jwt;

import com.github.sansarch.task_management.domain.user.model.UserId;
import com.github.sansarch.task_management.infrastructure.auth.config.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    private static final JwtProperties PROPERTIES =
            new JwtProperties("test-secret-value-must-be-at-least-32-bytes-long", 60);

    private final JwtService jwtService = new JwtService(PROPERTIES, FIXED_CLOCK);

    @Nested
    @DisplayName("generateToken() and parseUserId()")
    class RoundTrip {

        @Test
        @DisplayName("should round-trip to the same UserId")
        void shouldRoundTripToSameUserId() {
            UserId userId = UserId.generate();

            String token = jwtService.generateToken(userId, "jane@example.com");
            Optional<UserId> parsed = jwtService.parseUserId(token);

            assertThat(parsed).contains(userId);
        }
    }

    @Nested
    @DisplayName("parseUserId()")
    class ParseUserId {

        @Test
        @DisplayName("should return empty when token is signed with a different secret")
        void shouldReturnEmptyWhenSignedWithDifferentSecret() {
            JwtProperties otherProperties =
                    new JwtProperties("different-secret-value-must-be-at-least-32-bytes", 60);
            JwtService otherService = new JwtService(otherProperties, FIXED_CLOCK);
            String token = otherService.generateToken(UserId.generate(), "jane@example.com");

            Optional<UserId> parsed = jwtService.parseUserId(token);

            assertThat(parsed).isEmpty();
        }

        @Test
        @DisplayName("should return empty when token has expired")
        void shouldReturnEmptyWhenExpired() {
            String token = jwtService.generateToken(UserId.generate(), "jane@example.com");
            Clock afterExpiry = Clock.fixed(FIXED_INSTANT.plusSeconds(61L * 60), ZoneOffset.UTC);
            JwtService serviceAfterExpiry = new JwtService(PROPERTIES, afterExpiry);

            Optional<UserId> parsed = serviceAfterExpiry.parseUserId(token);

            assertThat(parsed).isEmpty();
        }

        @Test
        @DisplayName("should return empty when token is malformed")
        void shouldReturnEmptyWhenMalformed() {
            Optional<UserId> parsed = jwtService.parseUserId("not-a-valid-token");

            assertThat(parsed).isEmpty();
        }
    }
}
