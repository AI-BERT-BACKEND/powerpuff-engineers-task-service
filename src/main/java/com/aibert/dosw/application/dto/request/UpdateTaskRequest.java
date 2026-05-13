package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for editing an existing task (R39).
 * All fields are optional; only the fields provided (non-null) will be applied.
 * The same validation rules as {@code CreateTaskRequest} apply to each field when present.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    /** New title for the task. Must not be blank when provided. */
    private String title;

    /** New description for the task. */
    private String description;

    /** New subject identifier. Must not be blank when provided. */
    private String subjectId;

    /** New task type. */
    private TaskType taskType;

    /** New deadline. Must be in the future when provided. */
    @Future(message = "La fecha límite no puede ser en el pasado")
    private LocalDateTime deadline;

    /** New priority. */
    private TaskPriority priority;

    /** New estimated duration in minutes. Must be positive when provided. */
    @Positive(message = "La duración estimada debe ser positiva")
    private Integer estimatedDurationMinutes;
}
