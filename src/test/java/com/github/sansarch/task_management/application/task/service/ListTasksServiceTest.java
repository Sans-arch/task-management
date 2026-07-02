package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.out.TaskDomainRepository;
import com.github.sansarch.task_management.domain.task.model.Task;
import com.github.sansarch.task_management.domain.task.model.TaskId;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListTasksService")
class ListTasksServiceTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);

    @Mock
    private TaskDomainRepository taskDomainRepository;

    @InjectMocks
    private ListTasksService listTasksService;

    @Nested
    @DisplayName("list()")
    class List_ {

        @Test
        @DisplayName("should return a TaskResult for each task returned by the repository")
        void shouldReturnTaskResultsForEachTask() {
            TaskFilter filter = new TaskFilter(null, null);
            Task task1 = Task.reconstitute(TaskId.generate(), "Task 1", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            Task task2 = Task.reconstitute(TaskId.generate(), "Task 2", null, TaskStatus.IN_PROGRESS, TaskPriority.HIGH, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskDomainRepository.findAll(filter)).thenReturn(java.util.List.of(task1, task2));

            List<TaskResult> results = listTasksService.list(filter);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).title()).isEqualTo("Task 1");
            assertThat(results.get(1).title()).isEqualTo("Task 2");
        }

        @Test
        @DisplayName("should return an empty list when no tasks match the filter")
        void shouldReturnEmptyListWhenNoTasksFound() {
            TaskFilter filter = new TaskFilter(TaskStatus.DONE, null);
            when(taskDomainRepository.findAll(filter)).thenReturn(java.util.List.of());

            List<TaskResult> results = listTasksService.list(filter);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should map all TaskResult fields correctly")
        void shouldMapAllTaskResultFieldsCorrectly() {
            TaskFilter filter = new TaskFilter(null, null);
            TaskId id = TaskId.generate();
            Task task = Task.reconstitute(id, "Fix bug", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskDomainRepository.findAll(filter)).thenReturn(java.util.List.of(task));

            TaskResult result = listTasksService.list(filter).get(0);

            assertThat(result.id()).isEqualTo(id.id());
            assertThat(result.title()).isEqualTo("Fix bug");
            assertThat(result.description()).isEqualTo("Desc");
            assertThat(result.status()).isEqualTo(TaskStatus.TODO);
            assertThat(result.priority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(result.createdAt()).isEqualTo(FIXED_DATETIME);
            assertThat(result.updatedAt()).isEqualTo(FIXED_DATETIME);
        }

        @Test
        @DisplayName("should pass the filter to the repository")
        void shouldPassFilterToRepository() {
            TaskFilter filter = new TaskFilter(TaskStatus.TODO, TaskPriority.HIGH);
            when(taskDomainRepository.findAll(filter)).thenReturn(java.util.List.of());

            listTasksService.list(filter);

            verify(taskDomainRepository).findAll(filter);
        }
    }
}
