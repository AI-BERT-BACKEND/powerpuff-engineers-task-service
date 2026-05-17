package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.RescheduleTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.ConflictResponse;
import com.aibert.dosw.application.dto.response.DailySummaryResponse;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.DeleteTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetCalendarConflictsUseCase;
import com.aibert.dosw.domain.ports.in.GetDailySummaryUseCase;
import com.aibert.dosw.domain.ports.in.GetTaskByIdUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.in.RescheduleTaskUseCase;
import com.aibert.dosw.domain.ports.in.RestoreTaskUseCase;
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
import org.springframework.web.bind.annotation.PutMapping;
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
    private final RestoreTaskUseCase restoreTaskUseCase;
    private final GetTasksForViewUseCase getTasksForViewUseCase;
    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final RescheduleTaskUseCase rescheduleTaskUseCase;
    private final GetCalendarConflictsUseCase getCalendarConflictsUseCase;
    private final GetDailySummaryUseCase getDailySummaryUseCase;
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
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Subject not found"),
            @ApiResponse(responseCode = "409", description = "A task with the same title already exists for this subject")
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
     *   <li><b>No view param</b>: returns tasks sorted by {@code sortBy} criteria (default PRIORITY). Supports {@code limit}.</li>
     *   <li><b>view=kanban</b>: returns tasks grouped into a {@link KanbanResponse}.</li>
     *   <li><b>view=calendar</b>: returns tasks filtered by {@code status}, {@code startDate}, {@code endDate}, {@code subjectId}, {@code taskType}.</li>
     * </ul>
     *
     * @param studentId the student identifier from the {@code X-User-Id} header
     * @param sortBy    optional sort criteria; defaults to {@code PRIORITY} when absent
     * @param view      optional view type ({@code kanban} or {@code calendar})
     * @param status    optional status filter (used only for the calendar view)
     * @param startDate optional deadline lower bound in ISO date-time format
     * @param endDate   optional deadline upper bound in ISO date-time format
     * @param subjectId optional subject filter (used only for the calendar view)
     * @param taskType  optional task type filter (used only for the calendar view)
     * @param limit     optional max number of results (used only for the default sorted view)
     * @return HTTP 200 with a {@link KanbanResponse}, a {@code List<TaskResponse>}, or a sorted list
     */
    @GetMapping
    @Operation(summary = "Retrieve tasks for the authenticated student",
            description = "Returns the student's tasks. Without parameters: sorted list (supports 'limit=N'). With 'view=kanban': tasks grouped by status (TODO, IN_PROGRESS, PAUSED, COMPLETED). With 'view=calendar': tasks filtered by date range, status, subject, or task type.")
    @ApiResponse(responseCode = "200", description = "Task list retrieved successfully")
    public ResponseEntity<?> getTasks(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(required = false) SortCriteriaEnum sortBy,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) Integer limit) {

        if ("kanban".equalsIgnoreCase(view)) {
            Map<TaskStatus, List<Task>> grouped = getTasksForViewUseCase.getKanbanView(studentId);
            KanbanResponse kanban = KanbanResponse.builder()
                    .todo(grouped.getOrDefault(TaskStatus.TODO, List.of()).stream()
                            .map(taskDtoMapper::toResponse).toList())
                    .inProgress(grouped.getOrDefault(TaskStatus.IN_PROGRESS, List.of()).stream()
                            .map(taskDtoMapper::toResponse).toList())
                    .paused(grouped.getOrDefault(TaskStatus.PAUSED, List.of()).stream()
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

        List<Task> tasks = taskOrganizerUseCase.getOrganizedTasks(studentId, sortBy, limit);
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
    @Operation(summary = "Get task details by ID",
            description = "Returns the full details of a single task by its ID.")
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
    @Operation(summary = "Update the status of a task",
            description = "Updates the status of a task. Only the task owner can perform this action. When set to COMPLETED, the completion date is recorded and priorities are recalculated.")
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
     * Fully or partially updates an existing task (R15).
     * Accepts the same payload as PATCH. A 409 is returned if {@code scheduledDate}
     * overlaps with another task's scheduled window.
     *
     * @param id      the task identifier from the URL path
     * @param userId  the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @param request the update payload
     * @return HTTP 200 with the updated {@link TaskResponse}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a task (full or partial) — R15",
            description = "Updates editable fields of a task: title, description, deadline, subjectId, priority, taskType, estimatedDurationMinutes, scheduledDate. Only the owner may edit, and completed tasks cannot be modified. Returns 409 if scheduledDate overlaps with another task.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or task is completed"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "409", description = "scheduledDate overlaps with another task — response includes suggested date")
    })
    public ResponseEntity<TaskResponse> putTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task updatedTask = updateTaskUseCase.updateTask(id, userId, request);
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
    @Operation(summary = "Partially update a task",
            description = "Partially updates the fields of an existing task (title, description, deadline, priority, type, etc.). Only the owner may edit, and completed tasks cannot be modified.")
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
     * Soft-deletes a task (R16).
     * Sets {@code deletedAt} to the current timestamp. The task will no longer
     * appear in any listing. Only the owner may delete it.
     *
     * @param id     the task identifier from the URL path
     * @param userId the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task (soft-delete) — R16",
            description = "Marks the task as deleted by setting its deletedAt timestamp. The task is no longer visible in any listing. Only the task owner can delete it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        deleteTaskUseCase.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores a previously soft-deleted task (R16 — optional "Undo").
     * Only the task owner may restore it.
     *
     * @param id     the task identifier from the URL path
     * @param userId the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @return HTTP 200 with the restored {@link TaskResponse}
     */
    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted task — R16",
            description = "Clears the deletedAt timestamp, making the task visible again. Only the original task owner can restore it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task restored successfully"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No deleted task found with the given ID")
    })
    public ResponseEntity<TaskResponse> restoreTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        Task restored = restoreTaskUseCase.restoreTask(id, userId);
        return ResponseEntity.ok(taskDtoMapper.toResponse(restored));
    }

    /**
     * Returns all tasks for a given student (direct lookup by student ID).
     *
     * @param studentId the student identifier from the URL path
     * @return HTTP 200 with the list of {@link TaskResponse} objects
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get tasks by student",
            description = "Returns all tasks for a given student. The requesting user must match the student ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Requesting user does not match the student ID")
    })
    public ResponseEntity<?> getTasksByStudentId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String studentId) {
        if (!userId.equals(studentId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No tienes permiso para ver las tareas de otro estudiante"));
        }
        List<Task> tasks = getTasksUseCase.getTasksByStudentId(studentId);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a daily summary for the authenticated student.
     *
     * @param studentId the student identifier from the {@code X-User-Id} header
     * @return HTTP 200 with a {@link DailySummaryResponse}
     */
    @GetMapping("/daily-summary")
    @Operation(summary = "Resumen diario de tareas",
            description = "Retorna el porcentaje de completado, total de horas programadas, tareas completadas y pendientes para el día de hoy.")
    @ApiResponse(responseCode = "200", description = "Resumen diario obtenido exitosamente")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(getDailySummaryUseCase.getDailySummary(studentId));
    }

    /**
     * Returns active tasks (TODO or IN_PROGRESS) sorted by priority descending (AIB-19).
     *
     * @param studentId the student identifier from the {@code X-User-Id} header
     * @return HTTP 200 with a list of prioritized active {@link TaskResponse} objects
     */
    @GetMapping("/prioritized")
    @Operation(summary = "Tareas activas priorizadas (AIB-19)",
            description = "Retorna solo las tareas en estado TODO o IN_PROGRESS del estudiante, ordenadas de mayor a menor prioridad.")
    @ApiResponse(responseCode = "200", description = "Tareas priorizadas obtenidas exitosamente")
    public ResponseEntity<List<TaskResponse>> getPrioritizedActiveTasks(
            @RequestHeader("X-User-Id") String studentId) {
        List<Task> tasks = taskOrganizerUseCase.getPrioritizedActiveTasks(studentId);
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
            description = "Organizes and assigns dates to the student's pending tasks. The requesting user must match the student ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks organized successfully"),
            @ApiResponse(responseCode = "403", description = "Requesting user does not match the student ID")
    })
    public ResponseEntity<?> organizeTasks(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String studentId) {
        if (!userId.equals(studentId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No tienes permiso para organizar las tareas de otro estudiante"));
        }
        List<Task> organizedTasks = organizeTasksUseCase.organizeTasksForStudent(studentId);
        List<TaskResponse> response = organizedTasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Moves a task to a new date/time via drag-and-drop (R17 — interactive calendar).
     * Only updates {@code scheduledDate}; all other fields are preserved.
     * Returns 409 if the new date exceeds the deadline or overlaps another task.
     *
     * @param id               the task identifier from the URL path
     * @param userId           the student identifier forwarded by the API Gateway
     * @param request          payload containing the new {@code scheduledDate}
     * @return HTTP 200 with the rescheduled {@link TaskResponse}
     */
    @PatchMapping("/{id}/schedule")
    @Operation(summary = "Reschedule a task (drag-and-drop) — R17",
            description = "Updates only the scheduledDate of a task. Designed for drag-and-drop calendar interactions. Returns 409 if the new date exceeds the deadline or overlaps another task's time block.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task rescheduled successfully"),
            @ApiResponse(responseCode = "400", description = "scheduledDate is missing"),
            @ApiResponse(responseCode = "403", description = "Requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "409", description = "scheduledDate exceeds deadline or overlaps another task")
    })
    public ResponseEntity<TaskResponse> rescheduleTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RescheduleTaskRequest request) {
        Task rescheduled = rescheduleTaskUseCase.rescheduleTask(id, userId, request.getScheduledDate());
        return ResponseEntity.ok(taskDtoMapper.toResponse(rescheduled));
    }

    /**
     * Returns all time-block overlaps for the authenticated student (R17 — conflict highlighting).
     * The frontend uses this to render red-bordered blocks on the calendar.
     *
     * @param studentId the student identifier from the {@code X-User-Id} header
     * @param startDate optional lower bound for the conflict search window
     * @param endDate   optional upper bound for the conflict search window
     * @return HTTP 200 with a list of {@link ConflictResponse}; empty list when no conflicts exist
     */
    @GetMapping("/calendar/conflicts")
    @Operation(summary = "Get scheduled task conflicts (R17)",
            description = "Returns pairs of tasks whose time blocks overlap within the optional date range. Used by the frontend to highlight conflicting calendar blocks.")
    @ApiResponse(responseCode = "200", description = "Conflict list retrieved (empty if no overlaps)")
    public ResponseEntity<List<ConflictResponse>> getCalendarConflicts(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ConflictResponse> conflicts = getCalendarConflictsUseCase.getConflicts(studentId, startDate, endDate);
        return ResponseEntity.ok(conflicts);
    }
}
