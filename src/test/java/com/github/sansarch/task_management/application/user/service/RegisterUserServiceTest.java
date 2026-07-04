package com.github.sansarch.task_management.application.user.service;

import com.github.sansarch.task_management.application.user.dto.RegisterUserCommand;
import com.github.sansarch.task_management.application.user.dto.UserResult;
import com.github.sansarch.task_management.application.user.port.out.PasswordHasher;
import com.github.sansarch.task_management.application.user.port.out.UserGateway;
import com.github.sansarch.task_management.domain.user.exception.DuplicateEmailException;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserService")
class RegisterUserServiceTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should return a UserResult with the correct fields")
        void shouldReturnUserResultWithCorrectFields() {
            when(userGateway.findByEmail("jane@example.com")).thenReturn(Optional.empty());
            when(passwordHasher.hash("password123")).thenReturn("hashed-password");
            when(userGateway.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResult result = registerUserService.register(
                    new RegisterUserCommand("jane@example.com", "password123", "Jane Doe"));

            assertThat(result.email()).isEqualTo("jane@example.com");
            assertThat(result.displayName()).isEqualTo("Jane Doe");
            assertThat(result.id()).isNotNull();
        }

        @Test
        @DisplayName("should hash the password and persist the hash, not the raw password")
        void shouldPersistHashedPasswordNotRawPassword() {
            when(userGateway.findByEmail("jane@example.com")).thenReturn(Optional.empty());
            when(passwordHasher.hash("password123")).thenReturn("hashed-password");
            when(userGateway.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            registerUserService.register(new RegisterUserCommand("jane@example.com", "password123", "Jane Doe"));

            verify(userGateway).save(argThat(user -> user.getPasswordHash().equals("hashed-password")));
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email is already registered")
        void shouldThrowWhenEmailAlreadyRegistered() {
            when(userGateway.findByEmail("jane@example.com"))
                    .thenReturn(Optional.of(User.create("jane@example.com", "existing-hash", "Jane Doe")));

            assertThatThrownBy(() -> registerUserService.register(
                    new RegisterUserCommand("jane@example.com", "password123", "Jane Doe")))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("jane@example.com");

            verify(userGateway, never()).save(any());
        }
    }
}
