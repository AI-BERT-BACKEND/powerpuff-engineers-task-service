package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final GetTasksForViewUseCase getTasksForViewUseCase;
    private final TaskDtoMapper taskDtoMapper;

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

    @GetMapping
    @Operation(summary = "Get tasks (R12 and R13)",
            description = "Without 'view': returns sorted tasks (R12). With 'view=kanban': grouped by status. With 'view=calendar': filtered by date/status.")
    @ApiResponse(responseCode = "200", description = "Task list retrieved successfully")
    public ResponseEntity<?> getTasks(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(required = false) SortCriteriaEnum sortBy,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

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
            List<Task> tasks = getTasksForViewUseCase.getCalendarView(studentId, status, startDate, endDate);
            List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
            return ResponseEntity.ok(response);
        }

        List<Task> tasks = taskOrganizerUseCase.getOrganizedTasks(studentId, sortBy);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status (R13 - AC2, AC3)",
            description = "Updates the task status. When marked as COMPLETED, automatically records completedAt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Task updatedTask = updateTaskStatusUseCase.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get tasks by student",
            description = "Returns all tasks for a given student.")
    @ApiResponse(responseCode = "200", description = "Task list retrieved successfully")
    public ResponseEntity<List<TaskResponse>> getTasksByStudentId(@PathVariable String studentId) {
        List<Task> tasks = getTasksUseCase.getTasksByStudentId(studentId);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

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
