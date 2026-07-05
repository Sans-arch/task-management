package com.github.sansarch.task_management.application.task.service;

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

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteTaskService")
class DeleteTaskServiceTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);
    private static final UserId OWNER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));

    @Mock
    private TaskGateway taskDomainRepository;

    @Mock
    private TaskAuthorizationService taskAuthorizationService;

    @InjectMocks
    private DeleteTaskService deleteTaskService;

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete the task when it exists")
        void shouldDeleteTaskWhenItExists() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskDomainRepository.findById(taskId)).thenReturn(Optional.of(task));
            doNothing().when(taskAuthorizationService).assertCanManage(task);

            deleteTaskService.delete(taskId.id());

            verify(taskDomainRepository).delete(taskId);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task does not exist")
        void shouldThrowWhenTaskNotFound() {
            UUID id = UUID.randomUUID();
            when(taskDomainRepository.findById(new TaskId(id))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deleteTaskService.delete(id))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        @DisplayName("should not call delete when task is not found")
        void shouldNotCallDeleteWhenTaskNotFound() {
            UUID id = UUID.randomUUID();
            when(taskDomainRepository.findById(new TaskId(id))).thenReturn(Optional.empty());

            try {
                deleteTaskService.delete(id);
            } catch (TaskNotFoundException ignored) {
            }

            verify(taskDomainRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw TaskAccessDeniedException when caller cannot manage the task")
        void shouldThrowWhenAccessDenied() {
            TaskId taskId = TaskId.generate();
            Task task = Task.reconstitute(taskId, OWNER_ID, "Title", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskDomainRepository.findById(taskId)).thenReturn(Optional.of(task));
            doThrow(new TaskAccessDeniedException("denied")).when(taskAuthorizationService).assertCanManage(task);

            assertThatThrownBy(() -> deleteTaskService.delete(taskId.id()))
                    .isInstanceOf(TaskAccessDeniedException.class);

            verify(taskDomainRepository, never()).delete(any());
        }
    }
}
