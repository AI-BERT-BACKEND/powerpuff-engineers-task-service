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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "Endpoints de gestión de tareas")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTasksUseCase getTasksUseCase;
    private final OrganizeTasksUseCase organizeTasksUseCase;
    private final TaskOrganizerUseCase taskOrganizerUseCase;
    private final UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private final GetTasksForViewUseCase getTasksForViewUseCase;
    private final TaskDtoMapper taskDtoMapper;

    @PostMapping
    @Operation(summary = "Crear una nueva tarea", description = "Crea una nueva tarea con los detalles proporcionados y le asigna un estado inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task taskToCreate = taskDtoMapper.toModel(request);
        Task createdTask = createTaskUseCase.createTask(taskToCreate);
        return new ResponseEntity<>(taskDtoMapper.toResponse(createdTask), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener tareas (R12 y R13)",
            description = "Sin 'view': retorna tareas ordenadas (R12). Con 'view=kanban': agrupadas por estado. Con 'view=calendar': filtradas por fecha/estado.")
    @ApiResponse(responseCode = "200", description = "Lista de tareas obtenida exitosamente")
    public ResponseEntity<?> getTasks(
            @RequestParam String studentId,
            @RequestParam(required = false) SortCriteriaEnum sortBy,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if ("kanban".equalsIgnoreCase(view)) {
            KanbanResponse kanban = getTasksForViewUseCase.getKanbanView(studentId);
            return ResponseEntity.ok(kanban);
        }

        if ("calendar".equalsIgnoreCase(view)) {
            List<Task> tasks = getTasksForViewUseCase.getCalendarView(studentId, status, startDate, endDate);
            List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
            return ResponseEntity.ok(response);
        }

        // Default: R12 ordered tasks
        List<Task> tasks = taskOrganizerUseCase.getOrganizedTasks(studentId, sortBy);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de una tarea (R13 - AC2, AC3)",
            description = "Actualiza el estado de la tarea. Al marcar como COMPLETED, registra automáticamente completedAt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "400", description = "Estado inválido")
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Task updatedTask = updateTaskStatusUseCase.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtener tareas por estudiante", description = "Retorna todas las tareas de un estudiante.")
    @ApiResponse(responseCode = "200", description = "Lista de tareas obtenida exitosamente")
    public ResponseEntity<List<TaskResponse>> getTasksByStudentId(@PathVariable String studentId) {
        List<Task> tasks = getTasksUseCase.getTasksByStudentId(studentId);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student/{studentId}/organize")
    @Operation(summary = "Organizar tareas (Calendario)", description = "Organiza y asigna fechas a las tareas pendientes del estudiante.")
    @ApiResponse(responseCode = "200", description = "Tareas organizadas exitosamente")
    public ResponseEntity<List<TaskResponse>> organizeTasks(@PathVariable String studentId) {
        List<Task> organizedTasks = organizeTasksUseCase.organizeTasksForStudent(studentId);
        List<TaskResponse> response = organizedTasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
}

