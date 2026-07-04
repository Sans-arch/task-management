package com.github.sansarch.task_management.infrastructure.task.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.github.sansarch.task_management.application.shared.dto.PageResult;
import com.github.sansarch.task_management.application.task.dto.TaskResult;
import com.github.sansarch.task_management.application.task.port.in.CompleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.CreateTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.DeleteTaskUseCase;
import com.github.sansarch.task_management.application.task.port.in.ListTasksUseCase;
import com.github.sansarch.task_management.application.task.port.in.MarkTaskInProgressUseCase;
import com.github.sansarch.task_management.application.task.port.in.UpdateTaskUseCase;
import com.github.sansarch.task_management.domain.task.exception.InvalidTaskStateException;
import com.github.sansarch.task_management.domain.task.exception.TaskNotFoundException;
import com.github.sansarch.task_management.domain.task.model.TaskPriority;
import com.github.sansarch.task_management.domain.task.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController")
class TaskControllerIT {

    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDate FIXED_DATE = LocalDate.of(2025, Month.JANUARY, 8);
    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTaskUseCase createTaskUseCase;

    @MockitoBean
    private UpdateTaskUseCase updateTaskUseCase;

    @MockitoBean
    private DeleteTaskUseCase deleteTaskUseCase;

    @MockitoBean
    private ListTasksUseCase listTasksUseCase;

    @MockitoBean
    private MarkTaskInProgressUseCase markTaskInProgressUseCase;

    @MockitoBean
    private CompleteTaskUseCase completeTaskUseCase;

    private TaskResult sampleResult(TaskStatus status) {
        return new TaskResult(TASK_ID, "Title", "Description", status, TaskPriority.HIGH, FIXED_DATE, FIXED_DATETIME, FIXED_DATETIME);
    }

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTask {

        @Test
        @DisplayName("should return 201 with task response")
        void shouldReturn201() throws Exception {
            when(createTaskUseCase.create(any())).thenReturn(sampleResult(TaskStatus.TODO));

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "Title",
                                    "description", "Description",
                                    "status", "TODO",
                                    "priority", "HIGH"
                            ))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                    .andExpect(jsonPath("$.title").value("Title"))
                    .andExpect(jsonPath("$.status").value("TODO"))
                    .andExpect(jsonPath("$.priority").value("HIGH"));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "",
                                    "status", "TODO",
                                    "priority", "HIGH"
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when status is missing")
        void shouldReturn400WhenStatusIsMissing() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "Title",
                                    "priority", "HIGH"
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when priority is missing")
        void shouldReturn400WhenPriorityIsMissing() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "Title",
                                    "status", "TODO"
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class ListTasks {

        @Test
        @DisplayName("should return 200 with a page of tasks")
        void shouldReturn200WithList() throws Exception {
            when(listTasksUseCase.list(any(), any()))
                    .thenReturn(new PageResult<>(List.of(sampleResult(TaskStatus.TODO)), 0, 20, 1, 1));

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(TASK_ID.toString()))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("should return 200 with an empty page")
        void shouldReturn200WithEmptyList() throws Exception {
            when(listTasksUseCase.list(any(), any()))
                    .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("should return 200 when filtering by status and priority")
        void shouldReturn200WithFilters() throws Exception {
            when(listTasksUseCase.list(any(), any()))
                    .thenReturn(new PageResult<>(List.of(sampleResult(TaskStatus.TODO)), 0, 20, 1, 1));

            mockMvc.perform(get("/api/tasks")
                            .param("status", "TODO")
                            .param("priority", "HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        @DisplayName("should return 200 when passing page, size, sortBy and sortDirection")
        void shouldReturn200WithPaginationParams() throws Exception {
            when(listTasksUseCase.list(any(), any()))
                    .thenReturn(new PageResult<>(List.of(sampleResult(TaskStatus.TODO)), 1, 5, 6, 2));

            mockMvc.perform(get("/api/tasks")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sortBy", "TITLE")
                            .param("sortDirection", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalElements").value(6))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @DisplayName("should return 400 when page is negative")
        void shouldReturn400WhenPageIsNegative() throws Exception {
            mockMvc.perform(get("/api/tasks").param("page", "-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when size exceeds the maximum")
        void shouldReturn400WhenSizeExceedsMax() throws Exception {
            mockMvc.perform(get("/api/tasks").param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when sortBy is not a valid field")
        void shouldReturn400WhenSortByIsInvalid() throws Exception {
            mockMvc.perform(get("/api/tasks").param("sortBy", "NOT_A_FIELD"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/{id}")
    class UpdateTask {

        @Test
        @DisplayName("should return 200 with updated task")
        void shouldReturn200() throws Exception {
            when(updateTaskUseCase.update(any())).thenReturn(sampleResult(TaskStatus.TODO));

            mockMvc.perform(put("/api/tasks/{id}", TASK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "Updated",
                                    "status", "TODO",
                                    "priority", "HIGH"
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TASK_ID.toString()));
        }

        @Test
        @DisplayName("should return 404 when task does not exist")
        void shouldReturn404() throws Exception {
            when(updateTaskUseCase.update(any())).thenThrow(new TaskNotFoundException("Task not found: " + TASK_ID));

            mockMvc.perform(put("/api/tasks/{id}", TASK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "Updated",
                                    "status", "TODO",
                                    "priority", "HIGH"
                            ))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            mockMvc.perform(put("/api/tasks/{id}", TASK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "title", "",
                                    "status", "TODO",
                                    "priority", "HIGH"
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {

        @Test
        @DisplayName("should return 204 when task is deleted")
        void shouldReturn204() throws Exception {
            doNothing().when(deleteTaskUseCase).delete(TASK_ID);

            mockMvc.perform(delete("/api/tasks/{id}", TASK_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when task does not exist")
        void shouldReturn404() throws Exception {
            doThrow(new TaskNotFoundException("Task not found: " + TASK_ID)).when(deleteTaskUseCase).delete(TASK_ID);

            mockMvc.perform(delete("/api/tasks/{id}", TASK_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{id}/start")
    class MarkInProgress {

        @Test
        @DisplayName("should return 200 with IN_PROGRESS task")
        void shouldReturn200() throws Exception {
            when(markTaskInProgressUseCase.markInProgress(TASK_ID)).thenReturn(sampleResult(TaskStatus.IN_PROGRESS));

            mockMvc.perform(patch("/api/tasks/{id}/start", TASK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("should return 404 when task does not exist")
        void shouldReturn404() throws Exception {
            when(markTaskInProgressUseCase.markInProgress(TASK_ID))
                    .thenThrow(new TaskNotFoundException("Task not found: " + TASK_ID));

            mockMvc.perform(patch("/api/tasks/{id}/start", TASK_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("should return 400 when task state transition is invalid")
        void shouldReturn400WhenInvalidState() throws Exception {
            when(markTaskInProgressUseCase.markInProgress(TASK_ID))
                    .thenThrow(new InvalidTaskStateException("Cannot mark task as in progress from status: DONE"));

            mockMvc.perform(patch("/api/tasks/{id}/start", TASK_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{id}/complete")
    class CompleteTask {

        @Test
        @DisplayName("should return 200 with DONE task")
        void shouldReturn200() throws Exception {
            when(completeTaskUseCase.complete(TASK_ID)).thenReturn(sampleResult(TaskStatus.DONE));

            mockMvc.perform(patch("/api/tasks/{id}/complete", TASK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DONE"));
        }

        @Test
        @DisplayName("should return 404 when task does not exist")
        void shouldReturn404() throws Exception {
            when(completeTaskUseCase.complete(TASK_ID))
                    .thenThrow(new TaskNotFoundException("Task not found: " + TASK_ID));

            mockMvc.perform(patch("/api/tasks/{id}/complete", TASK_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("should return 400 when task is already DONE")
        void shouldReturn400WhenAlreadyDone() throws Exception {
            when(completeTaskUseCase.complete(TASK_ID))
                    .thenThrow(new InvalidTaskStateException("Task is already completed"));

            mockMvc.perform(patch("/api/tasks/{id}/complete", TASK_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }
}
