package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.DailySummaryResponse;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.DeleteTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetDailySummaryUseCase;
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
import org.springframework.web.bind.annotation.PutMapping;
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
@Tag(name = "Tareas", description = "Endpoints de gestión de tareas")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTasksUseCase getTasksUseCase;
    private final OrganizeTasksUseCase organizeTasksUseCase;
    private final TaskOrganizerUseCase taskOrganizerUseCase;
    private final UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final GetTasksForViewUseCase getTasksForViewUseCase;
    private final GetDailySummaryUseCase getDailySummaryUseCase;
    private final TaskDtoMapper taskDtoMapper;

    @PostMapping
    @Operation(summary = "Crear una nueva tarea",
            description = "Crea una nueva tarea con los detalles proporcionados y le asigna un estado inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<TaskResponse> createTask(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateTaskRequest request) {
        Task taskToCreate = taskDtoMapper.toModel(request, userId);
        Task createdTask = createTaskUseCase.createTask(taskToCreate);
        return new ResponseEntity<>(taskDtoMapper.toResponse(createdTask), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener tareas (R12 y R13)",
            description = "Sin 'view': retorna tareas ordenadas (R12). Con 'view=kanban': agrupadas por estado. Con 'view=calendar': filtradas por fecha/estado. Con 'limit=N': retorna solo las N primeras tareas.")
    @ApiResponse(responseCode = "200", description = "Lista de tareas obtenida exitosamente")
    public ResponseEntity<?> getTasks(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam(required = false) SortCriteriaEnum sortBy,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
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
            List<Task> tasks = getTasksForViewUseCase.getCalendarView(studentId, status, startDate, endDate);
            List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
            return ResponseEntity.ok(response);
        }

        List<Task> tasks = taskOrganizerUseCase.getOrganizedTasks(studentId, sortBy, limit);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de una tarea (R13 - AC2, AC3)",
            description = "Actualiza el estado de la tarea. Al marcar como COMPLETED, registra automáticamente completedAt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para actualizar esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "400", description = "Estado inválido")
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Task updatedTask = updateTaskStatusUseCase.updateStatus(id, userId, request.getStatus());
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar una tarea (AIB-18.2)",
            description = "Modifica los campos de una tarea existente. No se puede editar una tarea con estado 'Completada'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarea actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o tarea completada"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para editar esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task updatedTask = updateTaskUseCase.updateTask(id, userId, request);
        return ResponseEntity.ok(taskDtoMapper.toResponse(updatedTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una tarea (AIB-18.3)",
            description = "Elimina permanentemente una tarea. Solo el propietario puede eliminarla.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarea eliminada exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar esta tarea"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<Void> deleteTask(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        deleteTaskUseCase.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una tarea (AIB-20)",
            description = "Retorna el detalle completo de una tarea por su ID. Usado al hacer clic en una tarjeta del tablero Kanban.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarea obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String id) {
        Task task = getTasksUseCase.getTaskById(id);
        return ResponseEntity.ok(taskDtoMapper.toResponse(task));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtener tareas por estudiante",
            description = "Retorna todas las tareas de un estudiante.")
    @ApiResponse(responseCode = "200", description = "Lista de tareas obtenida exitosamente")
    public ResponseEntity<List<TaskResponse>> getTasksByStudentId(@PathVariable String studentId) {
        List<Task> tasks = getTasksUseCase.getTasksByStudentId(studentId);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily-summary")
    @Operation(summary = "Resumen diario de tareas",
            description = "Retorna el porcentaje de completado, total de horas programadas, tareas completadas y pendientes para el día de hoy.")
    @ApiResponse(responseCode = "200", description = "Resumen diario obtenido exitosamente")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(getDailySummaryUseCase.getDailySummary(studentId));
    }

    @GetMapping("/prioritized")
    @Operation(summary = "Tareas activas priorizadas (AIB-19)",
            description = "Retorna solo las tareas en estado TODO o IN_PROGRESS del estudiante, ordenadas de mayor a menor prioridad. Se invoca automáticamente para el organizador inteligente.")
    @ApiResponse(responseCode = "200", description = "Tareas priorizadas obtenidas exitosamente")
    public ResponseEntity<List<TaskResponse>> getPrioritizedActiveTasks(
            @RequestHeader("X-User-Id") String studentId) {
        List<Task> tasks = taskOrganizerUseCase.getPrioritizedActiveTasks(studentId);
        List<TaskResponse> response = tasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student/{studentId}/organize")
    @Operation(summary = "Organizar tareas (Calendario)",
            description = "Organiza y asigna fechas a las tareas pendientes del estudiante.")
    @ApiResponse(responseCode = "200", description = "Tareas organizadas exitosamente")
    public ResponseEntity<List<TaskResponse>> organizeTasks(@PathVariable String studentId) {
        List<Task> organizedTasks = organizeTasksUseCase.organizeTasksForStudent(studentId);
        List<TaskResponse> response = organizedTasks.stream().map(taskDtoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
}
