package com.github.sansarch.task_management.domain.task.model;

import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Task")
class TaskTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);
    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 1, 8);

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create task with correct fields")
        void shouldCreateTaskWithCorrectFields() {
            LocalDate dueDate = FIXED_DATE;

            Task task = Task.create("Fix bug", "Some description", TaskStatus.TODO, TaskPriority.MEDIUM, dueDate);

            assertThat(task.getTitle()).isEqualTo("Fix bug");
            assertThat(task.getDescription()).isEqualTo("Some description");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(task.getDueDate()).isEqualTo(dueDate);
        }

        @Test
        @DisplayName("should generate id and timestamps automatically")
        void shouldGenerateIdAndTimestampsAutomatically() {
            Task task = Task.create("Fix bug", null, TaskStatus.TODO, TaskPriority.LOW, null);

            assertThat(task.getId()).isNotNull();
            assertThat(task.getCreatedAt()).isNotNull();
            assertThat(task.getUpdatedAt()).isNotNull();
            assertThat(task.getCreatedAt()).isEqualTo(task.getUpdatedAt());
        }

        @Test
        @DisplayName("should allow null description and due date")
        void shouldAllowNullDescriptionAndDueDate() {
            Task task = Task.create("Fix bug", null, TaskStatus.TODO, TaskPriority.LOW, null);

            assertThat(task.getDescription()).isNull();
            assertThat(task.getDueDate()).isNull();
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        @DisplayName("should reconstitute with all provided fields")
        void shouldReconstituteWithAllProvidedFields() {
            TaskId id = TaskId.generate();
            LocalDate dueDate = FIXED_DATE;
            LocalDateTime createdAt = FIXED_DATETIME;
            LocalDateTime updatedAt = FIXED_DATETIME.plusDays(1);

            Task task = Task.reconstitute(id, "Title", "Desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                    dueDate, createdAt, updatedAt);

            assertThat(task.getId()).isEqualTo(id);
            assertThat(task.getTitle()).isEqualTo("Title");
            assertThat(task.getDescription()).isEqualTo("Desc");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
            assertThat(task.getDueDate()).isEqualTo(dueDate);
            assertThat(task.getCreatedAt()).isEqualTo(createdAt);
            assertThat(task.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("should throw when title is null")
        void shouldThrowWhenTitleIsNull() {
            assertThatThrownBy(() -> Task.create(null, "Desc", TaskStatus.TODO, TaskPriority.LOW, null))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("title");
        }

        @Test
        @DisplayName("should throw when title is blank")
        void shouldThrowWhenTitleIsBlank() {
            assertThatThrownBy(() -> Task.create("   ", "Desc", TaskStatus.TODO, TaskPriority.LOW, null))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("title");
        }

        @Test
        @DisplayName("should throw when title exceeds 255 characters")
        void shouldThrowWhenTitleExceeds255Characters() {
            String longTitle = "a".repeat(256);

            assertThatThrownBy(() -> Task.create(longTitle, "Desc", TaskStatus.TODO, TaskPriority.LOW, null))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("255");
        }

        @Test
        @DisplayName("should throw when status is null")
        void shouldThrowWhenStatusIsNull() {
            assertThatThrownBy(() -> Task.create("Title", "Desc", null, TaskPriority.LOW, null))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("status");
        }

        @Test
        @DisplayName("should throw when priority is null")
        void shouldThrowWhenPriorityIsNull() {
            assertThatThrownBy(() -> Task.create("Title", "Desc", TaskStatus.TODO, null, null))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("priority");
        }

        @Test
        @DisplayName("should throw when updatedAt is before createdAt")
        void shouldThrowWhenUpdatedAtIsBeforeCreatedAt() {
            LocalDateTime createdAt = FIXED_DATETIME;
            LocalDateTime updatedAt = FIXED_DATETIME.minusSeconds(1);

            assertThatThrownBy(() -> Task.reconstitute(TaskId.generate(), "Title", null,
                    TaskStatus.TODO, TaskPriority.LOW, null, createdAt, updatedAt))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("updatedAt");
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update mutable fields")
        void shouldUpdateMutableFields() {
            Task task = Task.create("Old title", "Old desc", TaskStatus.TODO, TaskPriority.LOW, null);
            LocalDate newDueDate = FIXED_DATE;

            task.update("New title", "New desc", TaskPriority.HIGH, newDueDate);

            assertThat(task.getTitle()).isEqualTo("New title");
            assertThat(task.getDescription()).isEqualTo("New desc");
            assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
            assertThat(task.getDueDate()).isEqualTo(newDueDate);
        }

        @Test
        @DisplayName("should bump updatedAt")
        void shouldBumpUpdatedAt() {
            Task task = Task.create("Title", "Desc", TaskStatus.TODO, TaskPriority.LOW, null);
            LocalDateTime before = task.getUpdatedAt();

            task.update("New title", "Desc", TaskPriority.LOW, null);

            assertThat(task.getUpdatedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("should not change status or createdAt")
        void shouldNotChangeStatusOrTimestamps() {
            Task task = Task.create("Title", "Desc", TaskStatus.TODO, TaskPriority.LOW, null);
            LocalDateTime createdAt = task.getCreatedAt();

            task.update("New title", "Desc", TaskPriority.LOW, null);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(task.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("should throw when updated title is blank")
        void shouldThrowWhenUpdatedTitleIsBlank() {
            Task task = Task.create("Title", "Desc", TaskStatus.TODO, TaskPriority.LOW, null);

            assertThatThrownBy(() -> task.update("", "Desc", TaskPriority.LOW, null))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("title");
        }
    }

    @Nested
    @DisplayName("markInProgress()")
    class MarkInProgress {

        @Test
        @DisplayName("should transition from TODO to IN_PROGRESS")
        void shouldTransitionFromTodo() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);

            task.markInProgress();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should throw when already IN_PROGRESS")
        void shouldThrowWhenAlreadyInProgress() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);
            task.markInProgress();

            assertThatThrownBy(task::markInProgress)
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("IN_PROGRESS");
        }

        @Test
        @DisplayName("should throw when already DONE")
        void shouldThrowWhenDone() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);
            task.complete();

            assertThatThrownBy(task::markInProgress)
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("DONE");
        }
    }

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("should transition from TODO to DONE")
        void shouldTransitionFromTodo() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);

            task.complete();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("should transition from IN_PROGRESS to DONE")
        void shouldTransitionFromInProgress() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);
            task.markInProgress();

            task.complete();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("should throw when already DONE")
        void shouldThrowWhenAlreadyDone() {
            Task task = Task.create("Title", null, TaskStatus.TODO, TaskPriority.LOW, null);
            task.complete();

            assertThatThrownBy(task::complete)
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("already completed");
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class Equality {

        @Test
        @DisplayName("should be equal when same id")
        void shouldBeEqualWhenSameId() {
            TaskId id = TaskId.generate();
            Task t1 = Task.reconstitute(id, "A", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            Task t2 = Task.reconstitute(id, "B", null, TaskStatus.DONE, TaskPriority.HIGH, null, FIXED_DATETIME, FIXED_DATETIME);

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when different id")
        void shouldNotBeEqualWhenDifferentId() {
            Task t1 = Task.reconstitute(TaskId.generate(), "A", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            Task t2 = Task.reconstitute(TaskId.generate(), "A", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);

            assertThat(t1).isNotEqualTo(t2);
        }
    }
}
