package com.github.sansarch.task_management.infrastructure.task.adapter.out.persistence;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.SortDirection;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.dto.TaskSortField;
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
class TaskRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, Month.JANUARY, 8);
    private static final TaskPageRequest DEFAULT_PAGE_REQUEST = new TaskPageRequest(0, 20, TaskSortField.CREATED_AT, SortDirection.DESC);

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

            PageResult<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(null, null), DEFAULT_PAGE_REQUEST);

            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return only tasks matching the status filter")
        void shouldFilterByStatus() {
            taskRepositoryAdapter.save(Task.create("Todo task", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Done task", null, TaskStatus.DONE, TaskPriority.LOW, null));

            PageResult<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(TaskStatus.TODO, null), DEFAULT_PAGE_REQUEST);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        }

        @Test
        @DisplayName("should return only tasks matching the priority filter")
        void shouldFilterByPriority() {
            taskRepositoryAdapter.save(Task.create("High task", null, TaskStatus.TODO, TaskPriority.HIGH, null));
            taskRepositoryAdapter.save(Task.create("Low task", null, TaskStatus.TODO, TaskPriority.LOW, null));

            PageResult<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(null, TaskPriority.HIGH), DEFAULT_PAGE_REQUEST);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
        }

        @Test
        @DisplayName("should return only tasks matching both status and priority filters")
        void shouldFilterByStatusAndPriority() {
            taskRepositoryAdapter.save(Task.create("Match", null, TaskStatus.TODO, TaskPriority.HIGH, null));
            taskRepositoryAdapter.save(Task.create("Wrong status", null, TaskStatus.DONE, TaskPriority.HIGH, null));
            taskRepositoryAdapter.save(Task.create("Wrong priority", null, TaskStatus.TODO, TaskPriority.LOW, null));

            PageResult<Task> result = taskRepositoryAdapter.findAll(new TaskFilter(TaskStatus.TODO, TaskPriority.HIGH), DEFAULT_PAGE_REQUEST);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getTitle()).isEqualTo("Match");
        }

        @Test
        @DisplayName("should return only the requested page slice, with correct pagination metadata")
        void shouldPaginateResults() {
            taskRepositoryAdapter.save(Task.create("Task A", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Task B", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Task C", null, TaskStatus.TODO, TaskPriority.LOW, null));

            TaskPageRequest firstPage = new TaskPageRequest(0, 2, TaskSortField.TITLE, SortDirection.ASC);
            PageResult<Task> firstResult = taskRepositoryAdapter.findAll(new TaskFilter(null, null), firstPage);

            assertThat(firstResult.content()).extracting(Task::getTitle).containsExactly("Task A", "Task B");
            assertThat(firstResult.page()).isEqualTo(0);
            assertThat(firstResult.size()).isEqualTo(2);
            assertThat(firstResult.totalElements()).isEqualTo(3);
            assertThat(firstResult.totalPages()).isEqualTo(2);

            TaskPageRequest secondPage = new TaskPageRequest(1, 2, TaskSortField.TITLE, SortDirection.ASC);
            PageResult<Task> secondResult = taskRepositoryAdapter.findAll(new TaskFilter(null, null), secondPage);

            assertThat(secondResult.content()).extracting(Task::getTitle).containsExactly("Task C");
        }

        @Test
        @DisplayName("should sort tasks by the requested field and direction")
        void shouldSortResults() {
            taskRepositoryAdapter.save(Task.create("Zebra", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Apple", null, TaskStatus.TODO, TaskPriority.LOW, null));
            taskRepositoryAdapter.save(Task.create("Mango", null, TaskStatus.TODO, TaskPriority.LOW, null));

            TaskPageRequest ascByTitle = new TaskPageRequest(0, 10, TaskSortField.TITLE, SortDirection.ASC);
            PageResult<Task> ascResult = taskRepositoryAdapter.findAll(new TaskFilter(null, null), ascByTitle);

            assertThat(ascResult.content()).extracting(Task::getTitle).containsExactly("Apple", "Mango", "Zebra");

            TaskPageRequest descByTitle = new TaskPageRequest(0, 10, TaskSortField.TITLE, SortDirection.DESC);
            PageResult<Task> descResult = taskRepositoryAdapter.findAll(new TaskFilter(null, null), descByTitle);

            assertThat(descResult.content()).extracting(Task::getTitle).containsExactly("Zebra", "Mango", "Apple");
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
