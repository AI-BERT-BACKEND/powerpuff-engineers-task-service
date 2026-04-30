package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizeTasksUseCaseImpl implements OrganizeTasksUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private static final int MAX_MINUTES_PER_DAY = 240;

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
        return allTasks;
    }

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
