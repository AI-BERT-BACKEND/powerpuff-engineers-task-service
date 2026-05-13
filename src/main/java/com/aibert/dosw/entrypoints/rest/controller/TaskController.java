package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.DeleteTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetTaskByIdUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing the task management API under {@code /api/tasks}.
 * Delegates all business logic to the corresponding use case input ports.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTasksUseCase getTasksUseCase;
    private final OrganizeTasksUseCase organizeTasksUseCase;
    private final TaskOrganizerUseCase taskOrganizerUseCase;
    private final UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final GetTasksForViewUseCase getTasksForViewUseCase;
    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final TaskDtoMapper taskDtoMapper;

    /**
     * Creates a new task for the authenticated student.
     *
     * @param userId  the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @param request the validated task creation payload
     * @return HTTP 201 with the created {@link TaskResponse}
     */
    @PostMapping
    @Operation(summary = "Create a new task",
            description = "Creates a new task with the provided details and assigns an initial status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TaskResponse> createTask(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateTaskRequest request) {
        Task taskToCreate = taskDtoMapper.toModel(request, userId);
        Task createdTask = createTaskUseCase.createTask(taskToCreate);
        return new ResponseEntity<>(taskDtoMapper.toResponse(createdTask), HttpStatus.CREATED);
    }

    /**
     * Retrieves tasks for the authenticated student, supporting three views:
     * <ul>
     *   <li><b>No view param</b>: returns tasks sorted by {@code sortBy} criteria (default PRIORITY).</li>
     *   <li><b>view=kanban</b>: returns tasks grouped into a {@link KanbanResponse}.</li>
     *   <li><b>view=calendar</b>: returns tasks filtered by {@code status}, {@code startDate}, {@code endDate}.</li>
     * </ul>
     *
     * @param studentId the student identifier from the {@code X-User-Id} header
     * @param sortBy    optional sort criteria; defaults to {@code PRIORITY} when absent
     * @param view      optional view type ({@code kanban} or {@code calendar})
     * @param status    optional status filter (used only for the calendar view)
     * @param startDate optional deadline lower bound in ISO date-time format
     * @param endDate   optional deadline upper bound in ISO date-time format
     * @return HTTP 200 with a {@link KanbanResponse}, a {@code List<TaskResponse>}, or a sorted list
     */
    @GetMapping
    @Operation(summary = "Get tasks (R12 and R13)",
            description = "Without 'view': returns sorted tasks (R12). With 'view=kanban': grouped by status. With 'view=calendar': filtered by date/status/subject/taskType.")
    @ApiResponse(responseCode = "200", description = "Task list retrieved successfully")
    public ResponseEntity<?> getTasks(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(required = false) SortCriteriaEnum sortBy,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) TaskType taskType) {

        if ("kanban".equalsIgnoreCase(view)) {
            Map<TaskStatus, List<Task>> grouped = getTasksForViewUseCase.getKanbanView(studentId);
            KanbanResponse kanban = KanbanResponse.builder()
                    .todo(grouped.getOrDefault(TaskStatus.TODO, List.of()).stream()
                            .map(taskDtoMapper::toResponse).toList())
                    .inProgress(grouped.getOrDefault(TaskStatus.IN_PROGRESS, List.of()).stream()
                            .map(taskDtoMapper::toResponse).toList())
                    .completed(grouped.getOrDefault(TaskStatus.COMPLETED, List.of()).stream()
                            .map(taskDtoMapper::toResponse).toList())
                    .build();
            return ResponseEntity.ok(kanban);
        }

        if ("calendar".equalsIgnoreCase(view)) {
            List<Task> tasks = getTasksForViewUseCase.getCalendarView(studentId, status, startDate, endDate,
                    subjectId, taskType);
            List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
            return ResponseEntity.ok(response);
        }

        List<Task> tasks = taskOrganizerUseCase.getOrganizedTasks(studentId, sortBy);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the detail of a single task by its ID (R41 - task detail on click).
     *
     * @param id the task identifier from the URL path
     * @return HTTP 200 with the {@link TaskResponse}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID (R41)",
            description = "Returns the full detail of a single task. Used for popover/detail view in the calendar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String id) {
        Task task = getTaskByIdUseCase.getTaskById(id);
        return ResponseEntity.ok(taskDtoMapper.toResponse(task));
    }

    /**
     * Updates the status of a specific task (R42).
     * Only the owner may change the status. Moving to {@code COMPLETED} records {@code completedAt}.
     * Any change triggers a priority recalculation for all remaining active tasks.
     *
     * @param id      the task identifier from the URL path
     * @param userId  the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @param request the validated status update payload
     * @return HTTP 200 with the updated {@link TaskResponse}
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status (R42)",
            description = "Updates the task status. Only the owner may change it. COMPLETED records completedAt and triggers priority recalculation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Task updatedTask = updateTaskStatusUseCase.updateStatus(id, userId, request.getStatus());
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    /**
     * Partially updates an existing task (R39).
     * Only non-null fields in the request body are applied.
     *
     * @param id      the task identifier from the URL path
     * @param userId  the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @param request the partial update payload
     * @return HTTP 200 with the updated {@link TaskResponse}
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Edit a task (R39)",
            description = "Partially updates a task's fields. Only the owner may edit, and completed tasks cannot be modified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or task is completed"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task updatedTask = updateTaskUseCase.updateTask(id, userId, request);
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    /**
     * Permanently deletes a task (R40).
     * The request must include {@code confirmed=true} as a guard against accidental deletion.
     *
     * @param id        the task identifier from the URL path
     * @param userId    the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @param confirmed must be {@code true}; returns HTTP 400 if {@code false}
     * @return HTTP 200 on success
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task (R40)",
            description = "Permanently deletes the task. The 'confirmed' parameter must be true. Only the owner may delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Deletion not confirmed"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Map<String, String>> deleteTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam boolean confirmed) {
        if (!confirmed) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La eliminación requiere confirmación explícita (confirmed=true)"));
        }
        deleteTaskUseCase.deleteTask(id, userId);
        return ResponseEntity.ok(Map.of("message", "Tarea eliminada correctamente"));
    }

    /**
     * Returns all tasks for a given student (direct lookup by student ID).
     *
     * @param studentId the student identifier from the URL path
     * @return HTTP 200 with the list of {@link TaskResponse} objects
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get tasks by student",
            description = "Returns all tasks for a given student.")
    @ApiResponse(responseCode = "200", description = "Task list retrieved successfully")
    public ResponseEntity<List<TaskResponse>> getTasksByStudentId(@PathVariable String studentId) {
        List<Task> tasks = getTasksUseCase.getTasksByStudentId(studentId);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Schedules (organizes) all pending ({@code TODO}) tasks for the given student
     * by assigning a {@code scheduledDate} to each one.
     *
     * @param studentId the student identifier from the URL path
     * @return HTTP 200 with all of the student's tasks (scheduled and non-pending)
     */
    @PostMapping("/student/{studentId}/organize")
    @Operation(summary = "Organize tasks (Calendar)",
            description = "Organizes and assigns dates to the student's pending tasks.")
    @ApiResponse(responseCode = "200", description = "Tasks organized successfully")
    public ResponseEntity<List<TaskResponse>> organizeTasks(@PathVariable String studentId) {
        List<Task> organizedTasks = organizeTasksUseCase.organizeTasksForStudent(studentId);
        List<TaskResponse> response = organizedTasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
}
