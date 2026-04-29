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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizeTasksUseCaseImpl implements OrganizeTasksUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private static final int MAX_MINUTES_PER_DAY = 240; // 4 hours max per day to avoid burnout

    @Override
    public List<Task> organizeTasksForStudent(String studentId) {
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        // Only organize TODO tasks
        List<Task> tasksToOrganize = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.TODO)
                .sorted(Comparator.comparing(this::getPriorityScore).reversed()
                        .thenComparing(Task::getDeadline))
                .collect(Collectors.toList());

        Map<LocalDate, Integer> dailyAssignedMinutes = new HashMap<>();
        LocalDate currentDate = LocalDate.now();

        for (Task task : tasksToOrganize) {
            LocalDate assignedDate = assignDateForTask(task, dailyAssignedMinutes, currentDate);
            task.setScheduledDate(LocalDateTime.of(assignedDate, LocalTime.of(9, 0)));
        }

        taskRepositoryPort.saveAll(tasksToOrganize);
        return allTasks;
    }

    private LocalDate assignDateForTask(Task task, Map<LocalDate, Integer> dailyAssignedMinutes, LocalDate startDate) {
        LocalDate dateIter = startDate;
        int duration = task.getEstimatedDurationMinutes() != null ? task.getEstimatedDurationMinutes() : 60;

        while (true) {
            int currentAssigned = dailyAssignedMinutes.getOrDefault(dateIter, 0);
            if (currentAssigned + duration <= MAX_MINUTES_PER_DAY) {
                if (dateIter.atStartOfDay().isBefore(task.getDeadline()) || dateIter.isEqual(task.getDeadline().toLocalDate())) {
                    dailyAssignedMinutes.put(dateIter, currentAssigned + duration);
                    return dateIter;
                } else if (dateIter.isAfter(task.getDeadline().toLocalDate())) {
                    LocalDate deadlineDay = task.getDeadline().toLocalDate();
                    if (deadlineDay.isBefore(startDate)) {
                        deadlineDay = startDate;
                    }
                    dailyAssignedMinutes.put(deadlineDay, dailyAssignedMinutes.getOrDefault(deadlineDay, 0) + duration);
                    return deadlineDay;
                }
            }
            dateIter = dateIter.plusDays(1);
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
