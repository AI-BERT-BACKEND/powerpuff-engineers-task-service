package com.aibert.dosw.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the Kanban board view.
 * Groups tasks into three columns based on their current status:
 * {@code todo}, {@code inProgress}, and {@code completed}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanResponse {
    private List<TaskResponse> todo;
    private List<TaskResponse> inProgress;
    private List<TaskResponse> paused;
    private List<TaskResponse> completed;
}
