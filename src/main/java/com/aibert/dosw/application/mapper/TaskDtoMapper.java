package com.aibert.dosw.application.mapper;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.domain.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoMapper {

    public Task toModel(CreateTaskRequest request) {
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .deadline(request.getDeadline())
                .priority(request.getPriority())
                .studentId(request.getStudentId())
                .subjectId(request.getSubjectId())
                .build();
    }

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
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
}
