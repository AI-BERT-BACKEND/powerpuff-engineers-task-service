package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model representing a student task.
 * <p>
 * Central aggregate of the task-service domain. Holds all information
 * needed to create, track, and schedule a task assigned to a student
 * for a specific academic subject.
 * </p>
 */
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
    private TaskType taskType;
    private Integer estimatedDurationMinutes;
    private LocalDateTime deadline;
    private TaskPriority priority;
    private TaskType taskType;
    private TaskStatus status;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedAt;
    private LocalDateTime deletedAt;
}
