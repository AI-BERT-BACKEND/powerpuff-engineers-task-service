package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskOrganizerServiceImpl implements TaskOrganizerUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria) {
        List<Task> tasks = taskRepositoryPort.findByStudentId(studentId);

        // AC3 & AC4: Recalculate priority — tasks with deadline in < 24h -> HIGH
        boolean tasksUpdated = false;
        LocalDateTime now = LocalDateTime.now();
        for (Task task : tasks) {
            if (task.getDeadline() != null) {
                long hoursUntilDeadline = ChronoUnit.HOURS.between(now, task.getDeadline());
                if (hoursUntilDeadline >= 0 && hoursUntilDeadline <= 24 && task.getPriority() != TaskPriority.CRITICAL) {
                    if (task.getPriority() != TaskPriority.HIGH) {
                        task.setPriority(TaskPriority.HIGH);
                        tasksUpdated = true;
                    }
                }
            }
        }

        if (tasksUpdated) {
            taskRepositoryPort.saveAll(tasks);
        }

        // AC1 & AC2: Sort based on criteria (default = PRIORITY)
        SortCriteriaEnum criteria = sortCriteria != null ? sortCriteria : SortCriteriaEnum.PRIORITY;

        return tasks.stream()
                .sorted(getComparator(criteria))
                .collect(Collectors.toList());
    }

    private Comparator<Task> getComparator(SortCriteriaEnum criteria) {
        switch (criteria) {
            case DEADLINE:
                return Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
            case SUBJECT:
                return Comparator.comparing(Task::getSubjectId, Comparator.nullsLast(String::compareToIgnoreCase));
            case PRIORITY:
            default:
                return Comparator.comparing(this::getPriorityScore).reversed()
                        .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
        }
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
