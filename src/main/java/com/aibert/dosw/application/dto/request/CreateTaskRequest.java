package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new task.
 * All validation constraints are enforced before the request reaches the use case layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Task type is required")
    private TaskType taskType;

    @Positive(message = "Estimated duration must be positive")
    @Max(value = 6000, message = "Estimated duration must not exceed 6000 minutes (100 hours)")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline cannot be in the past")
    private LocalDateTime deadline;

    private TaskPriority priority;

    @NotBlank(message = "Subject is required")
    private String subjectId;
}
