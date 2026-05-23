package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring service implementing the {@link GetTasksForViewUseCase} input port.
 * Provides task grouping for the Kanban view and filtered retrieval for the calendar view.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTasksForViewUseCaseImpl implements GetTasksForViewUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     * Fetches all tasks for the student and groups them by status using the stream API.
     *
     * @param studentId the student's identifier
     * @return a map of status → task list
     */
    @Override
    public Map<TaskStatus, List<Task>> getKanbanView(String studentId) {
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        // AIB-20 RN-03: only TODO, IN_PROGRESS and COMPLETED appear in the Kanban board
        Map<TaskStatus, List<Task>> kanban = allTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() != TaskStatus.PAUSED)
                .collect(Collectors.groupingBy(Task::getStatus));
        log.debug("KANBAN_VIEW | studentId={} | total={} | todo={} | inProgress={} | completed={}",
                studentId, allTasks.size(),
                kanban.getOrDefault(TaskStatus.TODO, List.of()).size(),
                kanban.getOrDefault(TaskStatus.IN_PROGRESS, List.of()).size(),
                kanban.getOrDefault(TaskStatus.COMPLETED, List.of()).size());
        return kanban;
    }

    /**
     * {@inheritDoc}
     * Delegates directly to the repository's filtered query.
     *
     * @param studentId the student's identifier
     * @param status    optional status filter
     * @param startDate optional deadline lower bound
     * @param endDate   optional deadline upper bound
     * @return filtered task list
     */
    @Override
    public List<Task> getCalendarView(String studentId, TaskStatus status,
                                      LocalDateTime startDate, LocalDateTime endDate,
                                      String subjectId, TaskType taskType) {
        List<Task> tasks = taskRepositoryPort.findByStudentIdWithFilters(studentId, status, startDate, endDate,
                subjectId, taskType);
        log.debug("CALENDAR_VIEW | studentId={} | status={} | subjectId={} | taskType={} | window=[{} - {}] | results={}",
                studentId, status, subjectId, taskType, startDate, endDate, tasks.size());
        return tasks;
    }
}
