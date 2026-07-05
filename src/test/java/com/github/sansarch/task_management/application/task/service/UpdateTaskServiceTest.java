package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.dto.UpdateTaskCommand;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.application.task.security.TaskAuthorizationService;
import com.github.sansarch.task_management.domain.task.exception.TaskAccessDeniedException;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import com.github.sansarch.task_management.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateTaskService")
class UpdateTaskServiceTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);
    private static final LocalDate FIXED_DUE_DATE = LocalDate.of(2025, Month.JANUARY, 8);
    private static final UserId OWNER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));

    @Mock
    private TaskGateway taskGateway;

    @Mock
    private TaskAuthorizationService taskAuthorizationService;

    @InjectMocks
    private UpdateTaskService updateTaskService;

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should return TaskResult with updated fields")
        void shouldReturnUpdatedTaskResult() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Old Title", "Old desc", TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            UpdateTaskCommand command = new UpdateTaskCommand(taskId.id(), "New Title", "New desc", TaskPriority.HIGH, FIXED_DUE_DATE);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doNothing().when(taskAuthorizationService).assertCanManage(task);
            when(taskGateway.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TaskResult result = updateTaskService.update(command);

            assertThat(result.title()).isEqualTo("New Title");
            assertThat(result.description()).isEqualTo("New desc");
            assertThat(result.priority()).isEqualTo(TaskPriority.HIGH);
            assertThat(result.dueDate()).isEqualTo(FIXED_DUE_DATE);
        }

        @Test
        @DisplayName("should preserve id and status after update")
        void shouldPreserveIdAndStatus() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.IN_PROGRESS, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            UpdateTaskCommand command = new UpdateTaskCommand(taskId.id(), "Updated", null, TaskPriority.MEDIUM, null);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doNothing().when(taskAuthorizationService).assertCanManage(task);
            when(taskGateway.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TaskResult result = updateTaskService.update(command);

            assertThat(result.id()).isEqualTo(taskId.id());
            assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task does not exist")
        void shouldThrowWhenTaskNotFound() {
            UUID id = UUID.randomUUID();
            when(taskGateway.findById(new TaskId(id))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> updateTaskService.update(new UpdateTaskCommand(id, "Title", null, TaskPriority.LOW, null)))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        @DisplayName("should throw TaskAccessDeniedException when caller cannot manage the task")
        void shouldThrowWhenAccessDenied() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doThrow(new TaskAccessDeniedException("denied")).when(taskAuthorizationService).assertCanManage(task);

            assertThatThrownBy(() -> updateTaskService.update(new UpdateTaskCommand(taskId.id(), "Title", null, TaskPriority.LOW, null)))
                    .isInstanceOf(TaskAccessDeniedException.class);

            verify(taskGateway, never()).save(any());
        }
    }
}
