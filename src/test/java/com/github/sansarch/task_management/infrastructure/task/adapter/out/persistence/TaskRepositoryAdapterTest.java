package com.github.sansarch.task_management.infrastructure.task.adapter.out.persistence;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import({TaskRepositoryAdapter.class, TaskMapper.class})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("TaskRepositoryAdapter")
class TaskRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, Month.JANUARY, 8);

    @Autowired
    private TaskRepositoryAdapter taskRepositoryAdapter;

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist and return the task with all fields")
        void shouldPersistAndReturnTask() {
            Task task = Task.create("Title", "Description", TaskStatus.TODO, TaskPriority.HIGH, FIXED_DATE);

            Task saved = taskRepositoryAdapter.save(task);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("Title");
            assertThat(saved.getDescription()).isEqualTo("Description");
            assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(saved.getPriority()).isEqualTo(TaskPriority.HIGH);
            assertThat(saved.getDueDate()).isEqualTo(FIXED_DATE);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return present Optional when task exists")
        void shouldReturnTaskWhenFound() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);
            taskRepositoryAdapter.save(task);

            Optional<Task> found = taskRepositoryAdapter.findById(task.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Title");
            assertThat(found.get().getId()).isEqualTo(task.getId());
        }

        @Test
        @DisplayName("should return empty Optional when task does not exist")
        void shouldReturnEmptyWhenNotFound() {
            Optional<Task> found = taskRepositoryAdapter.findById(TaskId.generate());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return all tasks when filter has no criteria")
        void shouldReturnAllTasksWithNoFilter() {
            taskRepositoryAdapter.save(Task.create("Task 1", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Task 2", null, TaskStatus.DONE, TaskPriority.HIGH, null));

            List<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(null, null));

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return only tasks matching the status filter")
        void shouldFilterByStatus() {
            taskRepositoryAdapter.save(Task.create("Todo task", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Done task", null, TaskStatus.DONE, TaskPriority.LOW, null));

            List<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(TaskStatus.TODO, null));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        }

        @Test
        @DisplayName("should return only tasks matching the priority filter")
        void shouldFilterByPriority() {
            taskRepositoryAdapter.save(Task.create("High task", null, TaskStatus.TODO, TaskPriority.HIGH, null));
            taskRepositoryAdapter.save(Task.create("Low task", null, TaskStatus.TODO, TaskPriority.LOW, null));

            List<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(null, TaskPriority.HIGH));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
        }

        @Test
        @DisplayName("should return only tasks matching both status and priority filters")
        void shouldFilterByStatusAndPriority() {
            taskRepositoryAdapter.save(Task.create("Match", null, TaskStatus.TODO, TaskPriority.HIGH, null));
            taskRepositoryAdapter.save(Task.create("Wrong status", null, TaskStatus.DONE, TaskPriority.HIGH, null));
            taskRepositoryAdapter.save(Task.create("Wrong priority", null, TaskStatus.TODO, TaskPriority.LOW, null));

            List<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(TaskStatus.TODO, TaskPriority.HIGH));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Match");
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should remove the task so it can no longer be found")
        void shouldDeleteTask() {
            Task task = Task.create("To be deleted", null, TaskStatus.TODO, TaskPriority.LOW, null);
            taskRepositoryAdapter.save(task);

            taskRepositoryAdapter.delete(task.getId());

            assertThat(taskRepositoryAdapter.findById(task.getId())).isEmpty();
        }
    }
}
