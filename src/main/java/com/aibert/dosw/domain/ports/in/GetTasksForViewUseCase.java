package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Input port for retrieving tasks formatted for specific UI views.
 */
public interface GetTasksForViewUseCase {

    /**
     * Groups all tasks for a student by their status for Kanban board display.
     *
     * @param studentId the student's identifier
     * @return a map from {@link com.aibert.dosw.domain.model.TaskStatus} to the list of tasks in that column
     */
    Map<TaskStatus, List<Task>> getKanbanView(String studentId);

    /**
     * Returns tasks for a student filtered by optional status, date range, subject and task type for calendar display.
     *
     * @param studentId the student's identifier
     * @param status    optional status filter; {@code null} to include all statuses
     * @param startDate optional lower bound for the task deadline (inclusive); {@code null} to skip
     * @param endDate   optional upper bound for the task deadline (inclusive); {@code null} to skip
     * @param subjectId optional subject identifier filter; {@code null} to include all subjects
     * @param taskType  optional task type filter; {@code null} to include all types
     * @return a filtered list of tasks matching the given criteria
     */
    List<Task> getCalendarView(String studentId, TaskStatus status,
                               LocalDateTime startDate, LocalDateTime endDate,
                               String subjectId, TaskType taskType);
}
