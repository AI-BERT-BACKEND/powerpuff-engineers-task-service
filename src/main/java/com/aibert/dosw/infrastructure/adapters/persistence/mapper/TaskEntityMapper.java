package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskEntityMapper {

    public TaskEntity toEntity(Task task) {
        return TaskEntity.builder()
                .id(task.getId())
                .studentId(task.getStudentId())
                .subjectId(task.getSubjectId())
                .title(task.getTitle())
                .description(task.getDescription())
                .estimatedDurationMinutes(task.getEstimatedDurationMinutes())
                .deadline(task.getDeadline())
                .priority(task.getPriority())
                .status(task.getStatus())
                .scheduledDate(task.getScheduledDate())
                .completedAt(task.getCompletedAt())
                .build();
    }

    public Task toDomain(TaskEntity entity) {
        return Task.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .subjectId(entity.getSubjectId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .estimatedDurationMinutes(entity.getEstimatedDurationMinutes())
                .deadline(entity.getDeadline())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .scheduledDate(entity.getScheduledDate())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
