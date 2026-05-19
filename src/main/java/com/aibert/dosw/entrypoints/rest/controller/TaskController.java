package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.RescheduleTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateDeadlineRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.ConflictResponse;
import com.aibert.dosw.application.dto.response.DailySummaryResponse;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.dto.response.UpdateTaskStatusResponse;
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
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.in.UpdateDeadlineUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Tasks", description = "Endpoints for creating, retrieving, updating and deleting academic tasks. Includes Kanban, calendar, and prioritized-list views.")
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
    private final RescheduleTaskUseCase rescheduleTaskUseCase;
    private final UpdateDeadlineUseCase updateDeadlineUseCase;
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
    @Operation(
        summary = "Create a task",
        description = "Creates a new academic task for the authenticated student. " +
                "The task is assigned an initial status of TODO. " +
                "Priority is set automatically if not provided: tasks with a deadline within the next 24 hours are escalated to HIGH; " +
                "all others default to MEDIUM. " +
                "After creation, urgency-based priority is recalculated for all other active tasks of the same student. " +
                "Returns the full task object including the generated identifier and assigned status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created — returns the full task including its generated ID and assigned status"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation — check field constraints (title max 200 chars, duration max 6000 min, deadline must be in the future)"),
            @ApiResponse(responseCode = "404", description = "The referenced subject does not exist"),
            @ApiResponse(responseCode = "409", description = "A task with the same title already exists for this subject and student"),
            @ApiResponse(responseCode = "422", description = "The referenced subject is not part of the student's active semester")
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
    @Operation(
        summary = "List tasks",
        description = "Returns tasks for the authenticated student. Behaviour depends on the optional `view` parameter:\n\n" +
                "**Default (no view):** Returns all tasks sorted by the `sortBy` criterion (defaults to PRIORITY). " +
                "Use `limit` to cap the number of results.\n\n" +
                "**view=kanban:** Returns tasks grouped into three columns — `todo`, `inProgress`, and `completed`. " +
                "Add `status` to show only one column.\n\n" +
                "**view=calendar:** Returns a flat list filtered by `status`, date range (`startDate`/`endDate`), `subjectId`, and `taskType`.")
    @ApiResponse(responseCode = "200", description = "Task list retrieved — returns a sorted list, a Kanban object, or a filtered list depending on the view parameter")
    public ResponseEntity<?> getTasks(
            @RequestHeader("X-User-Id") String studentId,
            @Parameter(description = "Sort criterion for the default view. Accepted values: PRIORITY (default), DEADLINE, SUBJECT.")
            @RequestParam(required = false) SortCriteriaEnum sortBy,
            @Parameter(description = "View mode. Accepted values: kanban, calendar. Omit for the default sorted list.")
            @RequestParam(required = false) String view,
            @Parameter(description = "Filter by task status. " +
                    "In the kanban view, accepted values are TODO, IN_PROGRESS, COMPLETED (shows only that column). " +
                    "In the calendar view, all status values are accepted: TODO, IN_PROGRESS, PAUSED, COMPLETED.")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Lower bound for the task deadline (ISO 8601, e.g. 2025-06-01T00:00:00). Calendar view only.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Upper bound for the task deadline (ISO 8601). Calendar view only.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Filter by subject identifier. Calendar view only.")
            @RequestParam(required = false) String subjectId,
            @Parameter(description = "Filter by task type. Calendar view only. Accepted values: TAREA, EXAMEN, PROYECTO, QUIZ, LECTURA, OTRO.")
            @RequestParam(required = false) TaskType taskType,
            @Parameter(description = "Maximum number of results to return. Default view only.")
            @RequestParam(required = false) Integer limit) {

        if ("kanban".equalsIgnoreCase(view)) {
            Map<TaskStatus, List<Task>> grouped = getTasksForViewUseCase.getKanbanView(studentId);
            // FA-02: optional status filter — empty the non-matching columns when a filter is active
            boolean showTodo      = status == null || status == TaskStatus.TODO;
            boolean showInProgress = status == null || status == TaskStatus.IN_PROGRESS;
            boolean showCompleted  = status == null || status == TaskStatus.COMPLETED;
            KanbanResponse kanban = KanbanResponse.builder()
                    .todo(showTodo
                            ? grouped.getOrDefault(TaskStatus.TODO, List.of()).stream()
                                    .map(taskDtoMapper::toResponse).toList()
                            : List.of())
                    .inProgress(showInProgress
                            ? grouped.getOrDefault(TaskStatus.IN_PROGRESS, List.of()).stream()
                                    .map(taskDtoMapper::toResponse).toList()
                            : List.of())
                    .completed(showCompleted
                            ? grouped.getOrDefault(TaskStatus.COMPLETED, List.of()).stream()
                                    .map(taskDtoMapper::toResponse).toList()
                            : List.of())
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
    @Operation(
        summary = "Get task by ID",
        description = "Returns the full details of a single task identified by its UUID. " +
                "Includes all fields: title, subject, type, priority, status, deadline, scheduled date, and timestamps.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found — returns the complete task object"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID")
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
     * @return HTTP 200 with {@link UpdateTaskStatusResponse} containing taskId, status, completedAt, changedAt, studentId and a confirmation message
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update task status",
        description = "Changes the status of a task. Only the task owner may perform this action. " +
                "Valid transitions: TODO → IN_PROGRESS → PAUSED → COMPLETED (and back where applicable). " +
                "When the task is set to COMPLETED, the completion timestamp is recorded automatically. " +
                "The response includes the new status, the timestamp of the change, and a confirmation message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated — returns task ID, student ID, new status, completion timestamp (if applicable), change timestamp, and a confirmation message"),
            @ApiResponse(responseCode = "400", description = "The provided status value is not valid"),
            @ApiResponse(responseCode = "403", description = "The requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID")
    })
    public ResponseEntity<UpdateTaskStatusResponse> updateTaskStatus(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Task updatedTask = updateTaskStatusUseCase.updateStatus(id, userId, request.getStatus());
        UpdateTaskStatusResponse response = UpdateTaskStatusResponse.builder()
                .taskId(updatedTask.getId())
                .studentId(updatedTask.getStudentId())
                .status(updatedTask.getStatus())
                .completedAt(updatedTask.getCompletedAt())
                .changedAt(updatedTask.getStatusChangedAt())
                .message("Tarea actualizada exitosamente")
                .build();
        return ResponseEntity.ok(response);
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
    @Operation(
        summary = "Replace task fields",
        description = "Updates all supplied fields of an existing task. Only the task owner may edit it. " +
                "Completed tasks cannot be modified. " +
                "Editable fields: title, description, subject, task type, priority, deadline, estimated duration, and scheduled date. " +
                "If the deadline is changed and no explicit priority is supplied, urgency-based priority escalation is applied automatically. " +
                "If the new scheduled date overlaps with another task's time block, a conflict error is returned along with a suggested alternative date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated — returns the full updated task object"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation, or the task is already completed and cannot be edited"),
            @ApiResponse(responseCode = "403", description = "The requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID"),
            @ApiResponse(responseCode = "409", description = "The requested scheduled date overlaps with another task — the response body includes a suggested conflict-free date")
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
    @Operation(
        summary = "Partially update a task",
        description = "Applies only the non-null fields supplied in the request body; all other fields remain unchanged. " +
                "Only the task owner may edit it, and completed tasks cannot be modified. " +
                "Same priority recalculation and scheduled-date overlap rules as the full update apply.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated — returns the full updated task object"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation, or the task is already completed and cannot be edited"),
            @ApiResponse(responseCode = "403", description = "The requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task updatedTask = updateTaskUseCase.updateTask(id, userId, request);
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    /**
     * Permanently deletes a task (AIB-18.3).
     * The deletion is irreversible. Only the owner may delete it.
     *
     * @param id     the task identifier from the URL path
     * @param userId the student identifier forwarded by the API Gateway via the {@code X-User-Id} header
     * @return HTTP 200 with a confirmation message
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a task",
        description = "Permanently removes the task. This action is irreversible — the task cannot be recovered after deletion. " +
                "Only the task owner may delete it. Returns a confirmation message on success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted — returns a JSON object with a confirmation message"),
            @ApiResponse(responseCode = "403", description = "The requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID")
    })
    public ResponseEntity<Map<String, String>> deleteTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        deleteTaskUseCase.deleteTask(id, userId);
        return ResponseEntity.ok(Map.of("message", "Tarea eliminada exitosamente"));
    }

    /**
     * Returns all tasks for a given student (direct lookup by student ID).
     *
     * @param studentId the student identifier from the URL path
     * @return HTTP 200 with the list of {@link TaskResponse} objects
     */
    @GetMapping("/student/{studentId}")
    @Operation(
        summary = "Get all tasks for a student",
        description = "Returns every task belonging to the given student. " +
                "The value in the `X-User-Id` header must match the `studentId` path variable; " +
                "students may only retrieve their own tasks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved — returns the complete list of the student's tasks"),
            @ApiResponse(responseCode = "403", description = "The requesting user's ID does not match the student ID in the path")
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
    @Operation(
        summary = "Get daily progress summary",
        description = "Returns a summary of the student's task progress for the current day. " +
                "Includes the overall completion percentage, total hours scheduled, " +
                "and counts of completed and pending tasks.")
    @ApiResponse(responseCode = "200", description = "Daily summary retrieved — returns completion percentage, total scheduled hours, completed count, and pending count")
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
    @Operation(
        summary = "Get prioritized active tasks",
        description = "Returns only the student's active tasks (TODO and IN_PROGRESS), sorted by priority from highest to lowest. " +
                "Tasks with a deadline within the next 24 hours are automatically escalated to CRITICAL before the list is returned. " +
                "Results are cached per student to avoid unnecessary recalculations. " +
                "Pass `forceRecalculate=true` to bypass the cache and force a fresh computation.")
    @ApiResponse(responseCode = "200", description = "Prioritized task list retrieved — returns active tasks sorted from CRITICAL down to LOW")
    public ResponseEntity<List<TaskResponse>> getPrioritizedActiveTasks(
            @RequestHeader("X-User-Id") String studentId,
            @Parameter(description = "When true, invalidates the cache and recalculates priorities before returning the list.")
            @RequestParam(name = "forceRecalculate", required = false, defaultValue = "false") boolean forzarRecalculo) {
        List<Task> tasks = taskOrganizerUseCase.getPrioritizedActiveTasks(studentId, forzarRecalculo);
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
    @Operation(
        summary = "Auto-schedule pending tasks",
        description = "Automatically assigns a scheduled date to each of the student's pending (TODO) tasks, " +
                "optimising them for the planner view. " +
                "Returns all tasks belonging to the student, including those that were already scheduled or completed. " +
                "The requesting user must match the student ID in the path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks auto-scheduled — returns the full updated task list for the student"),
            @ApiResponse(responseCode = "403", description = "The requesting user's ID does not match the student ID in the path")
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
    @Operation(
        summary = "Reschedule a task work session",
        description = "Updates the date and time when the student plans to work on the task (scheduled date). " +
                "Designed for planner drag-and-drop interactions. " +
                "The scheduled date must not exceed the task's deadline. " +
                "If the new time block overlaps with another scheduled task, a conflict error is returned " +
                "along with a suggested conflict-free date. " +
                "All other task fields remain unchanged.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work session rescheduled — returns the updated task with the new scheduled date"),
            @ApiResponse(responseCode = "400", description = "The new scheduled date was not provided"),
            @ApiResponse(responseCode = "403", description = "The requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID"),
            @ApiResponse(responseCode = "409", description = "The requested date exceeds the task deadline, or it overlaps another scheduled task — the response body includes a suggested alternative date")
    })
    public ResponseEntity<TaskResponse> rescheduleTask(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RescheduleTaskRequest request) {
        Task rescheduled = rescheduleTaskUseCase.rescheduleTask(id, userId, request.getScheduledDate());
        return ResponseEntity.ok(taskDtoMapper.toResponse(rescheduled));
    }

    /**
     * Updates the deadline of a task via calendar drag-and-drop (AIB-21 RN-02 / FA-03).
     * Persists the new due date and triggers urgency-based priority recalculation.
     *
     * @param id      the task identifier from the URL path
     * @param userId  the student identifier forwarded by the API Gateway
     * @param request payload containing the new {@code newDeadline}
     * @return HTTP 200 with the updated {@link TaskResponse}
     */
    @PatchMapping("/{id}/deadline")
    @Operation(
        summary = "Move task deadline",
        description = "Updates the due date of a task. Intended for calendar drag-and-drop interactions where the student " +
                "moves a task card to a different day. " +
                "After the deadline is updated, urgency-based priority escalation is applied automatically: " +
                "tasks whose new deadline falls within the next 24 hours are escalated to HIGH if not already HIGH or CRITICAL. " +
                "Completed tasks cannot have their deadline changed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deadline updated — returns the full updated task object with the new deadline and recalculated priority"),
            @ApiResponse(responseCode = "400", description = "The new deadline was not provided, or the task is already completed"),
            @ApiResponse(responseCode = "403", description = "The requesting student does not own this task"),
            @ApiResponse(responseCode = "404", description = "No task exists with the given ID")
    })
    public ResponseEntity<TaskResponse> updateDeadline(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateDeadlineRequest request) {
        Task updated = updateDeadlineUseCase.updateDeadline(id, userId, request.getNewDeadline());
        return ResponseEntity.ok(taskDtoMapper.toResponse(updated));
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
    @Operation(
        summary = "Detect scheduled time conflicts",
        description = "Returns every pair of tasks whose scheduled time blocks overlap for the authenticated student. " +
                "An overlap occurs when task A starts before task B ends and task B starts before task A ends. " +
                "Optionally restrict the search to a specific date range using `startDate` and `endDate`. " +
                "Returns an empty list when no conflicts exist. " +
                "The frontend uses this response to highlight conflicting blocks on the calendar.")
    @ApiResponse(responseCode = "200", description = "Conflict list retrieved — each entry contains the IDs, titles, and time windows of the two overlapping tasks; empty when no conflicts exist")
    public ResponseEntity<List<ConflictResponse>> getCalendarConflicts(
            @RequestHeader("X-User-Id") String studentId,
            @Parameter(description = "Start of the search window (ISO 8601). Only tasks scheduled on or after this date are checked.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End of the search window (ISO 8601). Only tasks scheduled on or before this date are checked.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ConflictResponse> conflicts = getCalendarConflictsUseCase.getConflicts(studentId, startDate, endDate);
        return ResponseEntity.ok(conflicts);
    }
}
