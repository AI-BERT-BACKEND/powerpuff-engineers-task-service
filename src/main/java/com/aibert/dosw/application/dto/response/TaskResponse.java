package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String studentId;
    private String subjectId;
    private String title;
    private String description;
    private Integer estimatedDurationMinutes;
    private LocalDateTime deadline;
    private TaskPriority priority;
    private TaskType taskType;
    private TaskStatus status;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedAt;
}
