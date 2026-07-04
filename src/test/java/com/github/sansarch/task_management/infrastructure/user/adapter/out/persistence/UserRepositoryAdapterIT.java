package com.github.sansarch.task_management.infrastructure.user.adapter.out.persistence;

import com.github.sansarch.task_management.domain.user.model.User;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@Import({UserRepositoryAdapter.class, UserMapper.class})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("UserRepositoryAdapter")
class UserRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist and return the user with all fields")
        void shouldPersistAndReturnUser() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");

            User saved = userRepositoryAdapter.save(user);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo("jane@example.com");
            assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(saved.getDisplayName()).isEqualTo("Jane Doe");
        }

        // The unique-email DB constraint is the last line of defense; in practice
        // RegisterUserService's findByEmail pre-check is what prevents this under normal
        // operation (a concurrent-registration race is possible and out of scope here).
        @Test
        @DisplayName("should throw when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {
            userRepositoryAdapter.save(User.create("jane@example.com", "hash-a", "Jane A"));

            // save() alone only queues the insert; the unique-constraint violation isn't raised
            // until the persistence context is flushed, so we force a flush to observe it here.
            assertThatThrownBy(() -> {
                userRepositoryAdapter.save(User.create("jane@example.com", "hash-b", "Jane B"));
                springDataUserRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return present Optional when user exists")
        void shouldReturnUserWhenFound() {
            User user = User.create("jane@example.com", "hashed-password", "Jane Doe");
            userRepositoryAdapter.save(user);

            Optional<User> found = userRepositoryAdapter.findById(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("jane@example.com");
            assertThat(found.get().getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("should return empty Optional when user does not exist")
        void shouldReturnEmptyWhenNotFound() {
            Optional<User> found = userRepositoryAdapter.findById(UserId.generate());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("should return present Optional when user exists")
        void shouldReturnUserWhenFound() {
            userRepositoryAdapter.save(User.create("jane@example.com", "hashed-password", "Jane Doe"));

            Optional<User> found = userRepositoryAdapter.findByEmail("jane@example.com");

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("should return empty Optional when no user has that email")
        void shouldReturnEmptyWhenNotFound() {
            Optional<User> found = userRepositoryAdapter.findByEmail("unknown@example.com");

            assertThat(found).isEmpty();
        }
    }
}
