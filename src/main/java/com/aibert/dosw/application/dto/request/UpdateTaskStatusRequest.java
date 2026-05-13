package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating the status of an existing task.
 * The {@code status} field is required and must be a valid {@link com.aibert.dosw.domain.model.TaskStatus} value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {

    @NotNull(message = "El estado es requerido")
    private TaskStatus status;
}
