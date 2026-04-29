package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    @Schema(description = "Identificador único de la tarea", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Identificador del estudiante propietario de la tarea", example = "S12345678")
    private String studentId;
    @Schema(description = "Identificador de la materia (subject) a la que pertenece la tarea", example = "MATH-101")
    private String subjectId;
    @Schema(description = "Título de la tarea", example = "Completar tarea de matemáticas")
    private String title;
    @Schema(description = "Descripción detallada de la tarea", example = "Ejercicios 1 a 10 del capítulo 4")
    private String description;
    @Schema(description = "Duración estimada para completar la tarea en minutos", example = "120")
    private Integer estimatedDurationMinutes;
    @Schema(description = "Fecha límite para finalizar la tarea", example = "2026-05-15T23:59:00")
    private LocalDateTime deadline;
    @Schema(description = "Nivel de prioridad de la tarea")
    private TaskPriority priority;
    @Schema(description = "Estado actual de la tarea")
    private TaskStatus status;
    @Schema(description = "Fecha y hora en la que la tarea está programada para realizarse", example = "2026-05-14T10:00:00")
    private LocalDateTime scheduledDate;
    @Schema(description = "Fecha y hora en la que la tarea fue completada", example = "2026-05-14T15:30:00")
    private LocalDateTime completedAt;
}
