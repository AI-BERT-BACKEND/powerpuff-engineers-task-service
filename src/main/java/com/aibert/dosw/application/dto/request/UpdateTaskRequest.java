package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /** New description for the task. */
    @Size(max = 500, message = "Description must not exceed 500 characters")
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

    /** New estimated duration in minutes. Must be positive and at most 6000 (100 h) when provided. */
    @Positive(message = "La duración estimada debe ser positiva")
    @Max(value = 6000, message = "Estimated duration must not exceed 6000 minutes (100 hours)")
    private Integer estimatedDurationMinutes;

    /**
     * Explicit scheduled date for the task. Must be present or future when provided.
     * If it overlaps with another task's scheduled window, a 409 Conflict is returned.
     */
    @FutureOrPresent(message = "La fecha programada no puede ser en el pasado")
    private LocalDateTime scheduledDate;
}
