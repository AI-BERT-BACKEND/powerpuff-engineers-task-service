package com.aibert.dosw.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanResponse {

    @Schema(description = "Tareas en estado Pendiente (TODO)")
    private List<TaskResponse> todo;

    @Schema(description = "Tareas en estado En Progreso (IN_PROGRESS)")
    private List<TaskResponse> inProgress;

    @Schema(description = "Tareas en estado Completada (COMPLETED)")
    private List<TaskResponse> completed;
}
