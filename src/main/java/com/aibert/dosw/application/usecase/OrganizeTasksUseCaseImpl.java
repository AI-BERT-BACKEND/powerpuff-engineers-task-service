package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring service implementing the {@link OrganizeTasksUseCase} input port.
 * Greedily assigns a {@code scheduledDate} to each pending task based on available daily capacity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizeTasksUseCaseImpl implements OrganizeTasksUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private static final int MAX_MINUTES_PER_DAY = 240;

    /**
     * {@inheritDoc}
     * <p>Filters only {@code TODO} tasks, sorts them by priority (descending) then deadline,
     * and assigns a start date at 09:00 on the earliest day with enough remaining capacity
     * ({@value MAX_MINUTES_PER_DAY} minutes per day). Falls back to the task's own deadline
     * if all earlier days are full.</p>
     *
     * @param studentId the student whose tasks should be organized
     * @return all tasks for the student (both newly scheduled and previously non-TODO tasks)
     */
    @Override
    public List<Task> organizeTasksForStudent(String studentId) {
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        List<Task> pendingTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.TODO)
                .sorted(Comparator.comparing(this::getPriorityScore).reversed()
                        .thenComparing(Task::getDeadline))
                .toList();

        Map<LocalDate, Integer> dailyAssignedMinutes = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Task task : pendingTasks) {
            LocalDate assignedDate = findAvailableDateForTask(task, dailyAssignedMinutes, today);
            task.setScheduledDate(LocalDateTime.of(assignedDate, LocalTime.of(9, 0)));
        }

        taskRepositoryPort.saveAll(pendingTasks);
        log.info("AUDIT | operation=AUTO_ORGANIZE | studentId={} | totalTasks={} | scheduledTasks={} | maxMinutesPerDay={}",
                studentId, allTasks.size(), pendingTasks.size(), MAX_MINUTES_PER_DAY);
        return allTasks;
    }

    /**
     * Finds the earliest calendar date on which the task can be scheduled
     * without exceeding the daily capacity limit.
     * <p>Iterates from {@code startDate} up to the task's effective deadline.
     * If all days in the range are full, the task is appended to the deadline day regardless.</p>
     *
     * @param task                  the task to schedule
     * @param dailyAssignedMinutes  mutable map tracking how many minutes have been assigned per day
     * @param startDate             the earliest date to consider (typically today)
     * @return the selected schedule date
     */
    private LocalDate findAvailableDateForTask(Task task,
                                               Map<LocalDate, Integer> dailyAssignedMinutes,
                                               LocalDate startDate) {
        int duration = task.getEstimatedDurationMinutes() != null
                ? task.getEstimatedDurationMinutes() : 60;
        LocalDate deadline = task.getDeadline().toLocalDate();
        LocalDate effectiveDeadline = deadline.isBefore(startDate) ? startDate : deadline;

        LocalDate candidate = startDate;
        while (!candidate.isAfter(effectiveDeadline)) {
            int assigned = dailyAssignedMinutes.getOrDefault(candidate, 0);
            if (assigned + duration <= MAX_MINUTES_PER_DAY) {
                dailyAssignedMinutes.put(candidate, assigned + duration);
                return candidate;
            }
            candidate = candidate.plusDays(1);
        }

        dailyAssignedMinutes.merge(effectiveDeadline, duration, Integer::sum);
        return effectiveDeadline;
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
