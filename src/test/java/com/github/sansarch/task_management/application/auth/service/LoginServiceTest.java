package com.github.sansarch.task_management.application.auth.service;

import com.github.sansarch.task_management.application.auth.dto.LoginCommand;
import com.github.sansarch.task_management.application.auth.dto.LoginResult;
import com.github.sansarch.task_management.application.auth.port.out.TokenIssuer;
import com.github.sansarch.task_management.application.user.port.out.PasswordHasher;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.exception.InvalidCredentialsException;
import com.github.sansarch.task_management.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService")
class LoginServiceTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private LoginService loginService;

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return a token from the TokenIssuer on successful login")
        void shouldReturnTokenOnSuccess() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");
            when(userGateway.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
            when(passwordHasher.matches("password123", "hashed-password")).thenReturn(true);
            when(tokenIssuer.issue(user.getId(), "jane@example.com")).thenReturn("jwt-token");
            when(tokenIssuer.expiresInSeconds()).thenReturn(3600L);

            LoginResult result = loginService.login(new LoginCommand("jane@example.com", "password123"));

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.email()).isEqualTo("jane@example.com");
            assertThat(result.expiresInSeconds()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException with a generic message when email is unknown")
        void shouldThrowGenericExceptionWhenEmailUnknown() {
            when(userGateway.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginService.login(new LoginCommand("unknown@example.com", "password123")))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("should throw the identical InvalidCredentialsException when password is wrong")
        void shouldThrowIdenticalExceptionWhenPasswordWrong() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");
            when(userGateway.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
            when(passwordHasher.matches("wrong-password", "hashed-password")).thenReturn(false);

            assertThatThrownBy(() -> loginService.login(new LoginCommand("jane@example.com", "wrong-password")))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("should never call the TokenIssuer when credentials are invalid")
        void shouldNeverIssueTokenOnInvalidCredentials() {
            when(userGateway.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginService.login(new LoginCommand("unknown@example.com", "password123")))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(tokenIssuer, never()).issue(any(), any());
        }
    }
}
