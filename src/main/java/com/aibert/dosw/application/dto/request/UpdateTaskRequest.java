package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String title;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;

    private String subjectId;

    private TaskType taskType;

    @Future(message = "La fecha límite no puede ser en el pasado")
    private LocalDateTime deadline;

    private TaskPriority priority;

    @Positive(message = "La duración estimada debe ser positiva")
    @Max(value = 6000, message = "La duración estimada no puede superar las 100 horas (6000 minutos)")
    private Integer estimatedDurationMinutes;
}
