package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Spring service implementing the {@link TaskOrganizerUseCase} input port.
 * Retrieves tasks for a student, optionally escalates priority for near-deadline tasks,
 * and returns the list sorted by the requested criteria.
 */
@Service
@RequiredArgsConstructor
public class TaskOrganizerServiceImpl implements TaskOrganizerUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private static final int DEADLINE_URGENCY_HOURS = 24;

    /**
     * {@inheritDoc}
     * <p>Only active tasks ({@code TODO} and {@code IN_PROGRESS}) are included in the result,
     * per R12 input scope. After escalation, applies a comparator built from {@code sortCriteria}.
     * When {@code sortCriteria} is {@code null}, defaults to {@code PRIORITY} ordering.</p>
     *
     * @param studentId    the student's identifier
     * @param sortCriteria the desired sort order; {@code null} defaults to {@code PRIORITY}
     * @return the sorted list of active tasks
     */
    @Override
    public List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria) {
        List<Task> tasks = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(java.util.stream.Collectors.toList());

        escalatePriorityForUrgentTasks(tasks);

        SortCriteriaEnum criteria = sortCriteria != null ? sortCriteria : SortCriteriaEnum.PRIORITY;

        return tasks.stream()
                .sorted(buildComparator(criteria))
                .toList();
    }

    /**
     * Escalates the priority of tasks whose deadline is within {@value DEADLINE_URGENCY_HOURS} hours
     * to {@code HIGH}, provided the task is not already {@code HIGH} or {@code CRITICAL}.
     * Persists the updated tasks in bulk if any escalation occurred.
     *
     * @param tasks the list of tasks to inspect and potentially escalate
     */
    private void escalatePriorityForUrgentTasks(List<Task> tasks) {
        LocalDateTime now = LocalDateTime.now();
        boolean anyUpdated = false;

        for (Task task : tasks) {
            if (task.getDeadline() == null) continue;

            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, task.getDeadline());
            boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
            boolean canEscalate = task.getPriority() != TaskPriority.CRITICAL
                    && task.getPriority() != TaskPriority.HIGH;

            if (isUrgent && canEscalate) {
                task.setPriority(TaskPriority.HIGH);
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            taskRepositoryPort.saveAll(tasks);
        }
    }

    /**
     * Builds a {@link Comparator} for tasks based on the given sort criteria.
     * <ul>
     *   <li>{@code DEADLINE}: ascending by deadline, nulls last.</li>
     *   <li>{@code SUBJECT}: ascending by subjectId (case-insensitive), nulls last.</li>
     *   <li>{@code PRIORITY}: descending by priority score, then ascending by deadline.</li>
     * </ul>
     *
     * @param criteria the sort criteria to apply
     * @return a {@link Comparator} instance for {@link Task}
     */
    private Comparator<Task> buildComparator(SortCriteriaEnum criteria) {
        return switch (criteria) {
            case DEADLINE -> Comparator.comparing(Task::getDeadline,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case SUBJECT -> Comparator.comparing(Task::getSubjectId,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case PRIORITY -> Comparator.comparing(this::getPriorityScore).reversed()
                    .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    /**
     * Maps a task's priority to a numeric score used for sorting.
     * A {@code null} priority returns {@code 0} (lowest possible score).
     *
     * @param task the task whose priority should be scored
     * @return a numeric score where CRITICAL=4, HIGH=3, MEDIUM=2, LOW=1, null=0
     */
    private int getPriorityScore(Task task) {
        if (task.getPriority() == null) return 0;
        return switch (task.getPriority()) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}
