package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implements the use cases for Kanban (AC1) and Calendar (AC4, AC5, AC6) views.
 */
@Service
@RequiredArgsConstructor
public class GetTasksForViewUseCaseImpl implements GetTasksForViewUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final TaskDtoMapper taskDtoMapper;

    /**
     * AC1: Returns tasks grouped by status (TODO, IN_PROGRESS, COMPLETED).
     */
    @Override
    public KanbanResponse getKanbanView(String studentId) {
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        List<TaskResponse> todo = allTasks.stream()
                .filter(t -> TaskStatus.TODO.equals(t.getStatus()))
                .map(taskDtoMapper::toResponse)
                .toList();

        List<TaskResponse> inProgress = allTasks.stream()
                .filter(t -> TaskStatus.IN_PROGRESS.equals(t.getStatus()))
                .map(taskDtoMapper::toResponse)
                .toList();

        List<TaskResponse> completed = allTasks.stream()
                .filter(t -> TaskStatus.COMPLETED.equals(t.getStatus()))
                .map(taskDtoMapper::toResponse)
                .toList();

        return KanbanResponse.builder()
                .todo(todo)
                .inProgress(inProgress)
                .completed(completed)
                .build();
    }

    /**
     * AC4, AC5, AC6: Returns tasks with optional status and date-range filters.
     */
    @Override
    public List<Task> getCalendarView(String studentId, TaskStatus status,
                                      LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepositoryPort.findByStudentIdWithFilters(studentId, status, startDate, endDate);
    }
}
