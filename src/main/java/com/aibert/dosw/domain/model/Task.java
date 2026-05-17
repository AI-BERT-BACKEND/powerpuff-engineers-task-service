package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String studentId;
    private String title;
    private String subjectId;
    private String description;
    private Integer estimatedDurationMinutes;
    private LocalDateTime deadline;
    private TaskPriority priority;
    private TaskType taskType;
    private TaskStatus status;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedAt;
}
