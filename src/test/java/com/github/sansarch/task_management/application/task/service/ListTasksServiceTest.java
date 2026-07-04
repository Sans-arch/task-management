package com.github.sansarch.task_management.application.task.service;

import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.SortDirection;
import com.github.sansarch.task_management.application.task.dto.TaskFilter;
import com.github.sansarch.task_management.application.task.dto.TaskPageRequest;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.dto.TaskSortField;
import com.github.sansarch.task_management.application.task.port.out.TaskGateway;
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
    private static final TaskPageRequest DEFAULT_PAGE_REQUEST = new TaskPageRequest(0, 20, TaskSortField.CREATED_AT, SortDirection.DESC);

    @Mock
    private TaskGateway taskDomainRepository;

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
            when(taskDomainRepository.findAll(filter, DEFAULT_PAGE_REQUEST))
                    .thenReturn(new PageResult<>(List.of(task1, task2), 0, 20, 2, 1));

            List<TaskResult> results = listTasksService.list(filter, DEFAULT_PAGE_REQUEST).content();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).title()).isEqualTo("Task 1");
            assertThat(results.get(1).title()).isEqualTo("Task 2");
        }

        @Test
        @DisplayName("should return an empty page when no tasks match the filter")
        void shouldReturnEmptyPageWhenNoTasksFound() {
            TaskFilter filter = new TaskFilter(TaskStatus.DONE, null);
            when(taskDomainRepository.findAll(filter, DEFAULT_PAGE_REQUEST))
                    .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

            PageResult<TaskResult> result = listTasksService.list(filter, DEFAULT_PAGE_REQUEST);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("should map all TaskResult fields correctly")
        void shouldMapAllTaskResultFieldsCorrectly() {
            TaskFilter filter = new TaskFilter(null, null);
            TaskId id = TaskId.generate();
            Task task = Task.reconstitute(id, "Fix bug", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskDomainRepository.findAll(filter, DEFAULT_PAGE_REQUEST))
                    .thenReturn(new PageResult<>(List.of(task), 0, 20, 1, 1));

            TaskResult result = listTasksService.list(filter, DEFAULT_PAGE_REQUEST).content().get(0);

            assertThat(result.id()).isEqualTo(id.id());
            assertThat(result.title()).isEqualTo("Fix bug");
            assertThat(result.description()).isEqualTo("Desc");
            assertThat(result.status()).isEqualTo(TaskStatus.TODO);
            assertThat(result.priority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(result.createdAt()).isEqualTo(FIXED_DATETIME);
            assertThat(result.updatedAt()).isEqualTo(FIXED_DATETIME);
        }

        @Test
        @DisplayName("should preserve the pagination metadata from the repository")
        void shouldPreservePaginationMetadata() {
            TaskFilter filter = new TaskFilter(null, null);
            TaskPageRequest pageRequest = new TaskPageRequest(1, 10, TaskSortField.TITLE, SortDirection.ASC);
            Task task = Task.reconstitute(TaskId.generate(), "Task", null, TaskStatus.TODO, TaskPriority.LOW, null, FIXED_DATETIME, FIXED_DATETIME);
            when(taskDomainRepository.findAll(filter, pageRequest))
                    .thenReturn(new PageResult<>(List.of(task), 1, 10, 25, 3));

            PageResult<TaskResult> result = listTasksService.list(filter, pageRequest);

            assertThat(result.page()).isEqualTo(1);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25);
            assertThat(result.totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("should pass the filter and page request to the repository")
        void shouldPassFilterAndPageRequestToRepository() {
            TaskFilter filter = new TaskFilter(TaskStatus.TODO, TaskPriority.HIGH);
            when(taskDomainRepository.findAll(filter, DEFAULT_PAGE_REQUEST))
                    .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

            listTasksService.list(filter, DEFAULT_PAGE_REQUEST);

            verify(taskDomainRepository).findAll(filter, DEFAULT_PAGE_REQUEST);
        }
    }
}
