package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for the AIB-18.4 status-change operation.
 * Includes the updated task state plus a confirmation message and the timestamp of the change.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusResponse {
    private String taskId;
    private String studentId;
    private TaskStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime changedAt;
    private String message;
}
