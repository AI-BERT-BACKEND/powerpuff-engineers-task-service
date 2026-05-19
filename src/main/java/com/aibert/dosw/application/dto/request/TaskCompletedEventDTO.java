package com.aibert.dosw.application.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * Payload sent to gamification-service when a student completes a task.
 * Used by {@code GamificationServiceClient}.
 */
@Getter
@Builder
public class TaskCompletedEventDTO {

    private String taskId;
    private String studentId;
    private String subjectId;
    private String taskType;
}
