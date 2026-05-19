package com.aibert.dosw.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the Kanban board view (AIB-20).
 * Groups tasks into exactly three columns per spec: todo, inProgress, completed.
 * PAUSED tasks are excluded from the Kanban board (RN-03).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanResponse {
    private List<TaskResponse> todo;
    private List<TaskResponse> inProgress;
    private List<TaskResponse> completed;
}
