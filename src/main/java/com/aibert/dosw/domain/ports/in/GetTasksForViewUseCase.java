package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.application.dto.response.KanbanResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inbound port for retrieving tasks formatted for Kanban and Calendar views (R13 - AC1, AC4, AC5, AC6).
 */
public interface GetTasksForViewUseCase {

    /**
     * Returns tasks grouped by status for the Kanban view (AC1).
     *
     * @param studentId the student identifier
     * @return tasks grouped by status (TODO, IN_PROGRESS, COMPLETED)
     */
    KanbanResponse getKanbanView(String studentId);

    /**
     * Returns tasks filtered for the Calendar view (AC4, AC5, AC6).
     *
     * @param studentId the student identifier
     * @param status    optional status filter (null = all statuses)
     * @param startDate optional start of deadline range
     * @param endDate   optional end of deadline range
     * @return filtered list of tasks with deadline info
     */
    List<Task> getCalendarView(String studentId, TaskStatus status,
                               LocalDateTime startDate, LocalDateTime endDate);
}
