package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {

    @NotNull(message = "El estado es requerido")
    @Schema(description = "Nuevo estado de la tarea", example = "COMPLETED")
    private TaskStatus status;
}
