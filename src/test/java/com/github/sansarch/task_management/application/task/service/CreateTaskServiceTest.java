package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.CreateTaskCommand;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.out.TaskRepository;
import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTaskService")
class CreateTaskServiceTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, Month.JANUARY, 8);

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CreateTaskService createTaskService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should return a TaskResult with the correct fields")
        void shouldReturnTaskResultWithCorrectFields() {
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            CreateTaskCommand command = new CreateTaskCommand(
                    "Fix bug", "Some description", TaskStatus.TODO, TaskPriority.MEDIUM, FIXED_DATE);

            TaskResult result = createTaskService.create(command);

            assertThat(result.title()).isEqualTo("Fix bug");
            assertThat(result.description()).isEqualTo("Some description");
            assertThat(result.status()).isEqualTo(TaskStatus.TODO);
            assertThat(result.priority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(result.dueDate()).isEqualTo(FIXED_DATE);
        }

        @Test
        @DisplayName("should generate a non-null id")
        void shouldGenerateNonNullId() {
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            CreateTaskCommand command = new CreateTaskCommand(
                    "Fix bug", null, TaskStatus.TODO, TaskPriority.LOW, null);

            TaskResult result = createTaskService.create(command);

            assertThat(result.id()).isNotNull();
        }

        @Test
        @DisplayName("should save the task via the repository")
        void shouldSaveTaskViaRepository() {
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            CreateTaskCommand command = new CreateTaskCommand(
                    "Fix bug", null, TaskStatus.TODO, TaskPriority.LOW, null);

            createTaskService.create(command);

            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should propagate domain validation exception when title is blank")
        void shouldPropagateDomainValidationException() {
            CreateTaskCommand command = new CreateTaskCommand(
                    "", null, TaskStatus.TODO, TaskPriority.LOW, null);

            assertThatThrownBy(() -> createTaskService.create(command))
                    .isInstanceOf(InvalidTaskStateException.class)
                    .hasMessageContaining("title");
        }
    }
}
