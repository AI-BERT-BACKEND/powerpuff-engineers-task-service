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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskOrganizerServiceImpl implements TaskOrganizerUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private static final int DEADLINE_URGENCY_HOURS = 24;
    private static final Set<TaskStatus> ACTIVE_STATUSES = Set.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS);

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

    @Override
    public List<Task> getPrioritizedActiveTasks(String studentId) {
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        List<Task> activeTasks = allTasks.stream()
                .filter(t -> t.getStatus() != null && ACTIVE_STATUSES.contains(t.getStatus()))
                .toList();

        escalatePriorityForUrgentTasks(activeTasks);

        return activeTasks.stream()
                .sorted(buildComparator(SortCriteriaEnum.PRIORITY))
                .toList();
    }

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
