package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
import com.github.sansarch.task_management.application.task.security.TaskAuthorizationService;
import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
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
@DisplayName("CompleteTaskService")
class CompleteTaskServiceTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);
    private static final UserId OWNER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));

    @Mock
    private TaskGateway taskGateway;

    @Mock
    private TaskAuthorizationService taskAuthorizationService;

    @InjectMocks
    private CompleteTaskService completeTaskService;

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("should return TaskResult with DONE status when task is TODO")
        void shouldCompleteFromTodo() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doNothing().when(taskAuthorizationService).assertCanManage(task);
            when(taskGateway.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TaskResult result = completeTaskService.complete(taskId.id());

            assertThat(result.status()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("should return TaskResult with DONE status when task is IN_PROGRESS")
        void shouldCompleteFromInProgress() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.IN_PROGRESS, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doNothing().when(taskAuthorizationService).assertCanManage(task);
            when(taskGateway.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TaskResult result = completeTaskService.complete(taskId.id());

            assertThat(result.status()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task does not exist")
        void shouldThrowWhenTaskNotFound() {
            UUID id = UUID.randomUUID();
            when(taskGateway.findById(new TaskId(id))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> completeTaskService.complete(id))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        @DisplayName("should throw InvalidTaskStateException when task is already DONE")
        void shouldThrowWhenTaskIsAlreadyDone() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.DONE, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doNothing().when(taskAuthorizationService).assertCanManage(task);

            assertThatThrownBy(() -> completeTaskService.complete(taskId.id()))
                    .isInstanceOf(InvalidTaskStateException.class);
        }

        @Test
        @DisplayName("should throw TaskAccessDeniedException when caller cannot manage the task")
        void shouldThrowWhenAccessDenied() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskGateway.findById(taskId)).thenReturn(Optional.of(task));
            doThrow(new TaskAccessDeniedException("denied")).when(taskAuthorizationService).assertCanManage(task);

            assertThatThrownBy(() -> completeTaskService.complete(taskId.id()))
                    .isInstanceOf(TaskAccessDeniedException.class);

            verify(taskGateway, never()).save(any());
        }
    }
}
