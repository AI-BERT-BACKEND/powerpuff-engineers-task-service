package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "El título es requerido")
    @Schema(description = "Título de la tarea", example = "Completar tarea de matemáticas")
    private String title;

    @Schema(description = "Descripción detallada de la tarea", example = "Ejercicios 1 a 10 del capítulo 4")
    private String description;

    @NotNull(message = "La duración estimada es requerida")
    @Positive(message = "La duración estimada debe ser positiva")
    @Schema(description = "Duración estimada para completar la tarea en minutos", example = "120")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "La fecha límite es requerida")
    @Future(message = "La fecha límite no puede ser en el pasado")
    @Schema(description = "Fecha límite para finalizar la tarea", example = "2026-05-15T23:59:00")
    private LocalDateTime deadline;

    @NotNull(message = "La prioridad es requerida")
    @Schema(description = "Nivel de prioridad de la tarea")
    private TaskPriority priority;
    
    @NotBlank(message = "El ID del estudiante es requerido")
    @Schema(description = "Identificador del estudiante propietario de la tarea", example = "S12345678")
    private String studentId;

    @NotBlank(message = "La materia es requerida")
    @Schema(description = "Identificador de la materia (subject) a la que pertenece la tarea", example = "MATH-101")
    private String subjectId;
}
