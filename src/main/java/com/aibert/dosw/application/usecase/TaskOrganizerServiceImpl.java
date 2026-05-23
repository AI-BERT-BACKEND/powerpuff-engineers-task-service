package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Spring service implementing the {@link TaskOrganizerUseCase} input port.
 * Retrieves tasks for a student, optionally escalates priority for near-deadline tasks,
 * and returns the list sorted by the requested criteria.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskOrganizerServiceImpl implements TaskOrganizerUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private static final int DEADLINE_URGENCY_HOURS = 24;
    private static final Set<TaskStatus> ACTIVE_STATUSES = Set.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS);

    /**
     * {@inheritDoc}
     * <p>Delegates to the overloaded method with {@code limit = null}.</p>
     *
     * @param studentId    the student's identifier
     * @param sortCriteria the desired sort order; {@code null} defaults to {@code PRIORITY}
     * @return the sorted list of tasks
     */
    @Override
    public List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria) {
        return getOrganizedTasks(studentId, sortCriteria, null);
    }

    @Override
    public List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria, Integer limit) {
        List<Task> tasks = taskRepositoryPort.findByStudentId(studentId);

        escalatePriorityForUrgentTasks(tasks);

        SortCriteriaEnum criteria = sortCriteria != null ? sortCriteria : SortCriteriaEnum.PRIORITY;

        List<Task> sorted = tasks.stream()
                .sorted(buildComparator(criteria))
                .toList();

        if (limit != null && limit > 0) {
            return sorted.stream().limit(limit).toList();
        }
        return sorted;
    }

    @Caching(
        cacheable = @Cacheable(value = "prioritizedTasks", key = "#studentId", condition = "!#forzarRecalculo"),
        put     = @CachePut( value = "prioritizedTasks", key = "#studentId", condition = "#forzarRecalculo")
    )
    @Override
    public List<Task> getPrioritizedActiveTasks(String studentId, boolean forzarRecalculo) {
        log.debug("PRIORITIZED_TASKS | studentId={} | cacheBypass={}", studentId, forzarRecalculo);
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        List<Task> activeTasks = allTasks.stream()
                .filter(t -> t.getStatus() != null && ACTIVE_STATUSES.contains(t.getStatus()))
                .toList();

        escalatePriorityForUrgentTasks(activeTasks);

        List<Task> sorted = activeTasks.stream()
                .sorted(buildComparator(SortCriteriaEnum.PRIORITY))
                .toList();
        log.debug("PRIORITIZED_TASKS | studentId={} | activeTasks={} | returned={}", studentId, activeTasks.size(), sorted.size());
        return sorted;
    }

    @Override
    public List<Task> getPrioritizedActiveTasks(String studentId) {
        return getPrioritizedActiveTasks(studentId, false);
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
        int escalatedCount = 0;

        for (Task task : tasks) {
            if (task.getDeadline() == null) continue;

            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, task.getDeadline());
            boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
            boolean canEscalate = task.getPriority() != TaskPriority.CRITICAL;

            if (isUrgent && canEscalate) {
                task.setPriority(TaskPriority.CRITICAL);
                anyUpdated = true;
                escalatedCount++;
            }
        }

        if (anyUpdated) {
            log.info("PRIORITY_ESCALATION | newlyEscalatedToCritical={} | reason=deadline_within_{}h",
                    escalatedCount, DEADLINE_URGENCY_HOURS);
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
