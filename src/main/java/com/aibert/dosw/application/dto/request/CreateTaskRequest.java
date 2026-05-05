package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "El título es requerido")
    private String title;

    private String description;

    @NotNull(message = "La duración estimada es requerida")
    @Positive(message = "La duración estimada debe ser positiva")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "La fecha límite es requerida")
    @Future(message = "La fecha límite no puede ser en el pasado")
    private LocalDateTime deadline;

    @NotNull(message = "La prioridad es requerida")
    private TaskPriority priority;

    @NotBlank(message = "La materia es requerida")
    private String subjectId;
}
