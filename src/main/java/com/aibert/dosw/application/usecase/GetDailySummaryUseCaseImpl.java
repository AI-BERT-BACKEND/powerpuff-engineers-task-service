package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.DailySummaryResponse;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.GetDailySummaryUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetDailySummaryUseCaseImpl implements GetDailySummaryUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public DailySummaryResponse getDailySummary(String studentId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<Task> todayTasks = taskRepositoryPort.findByStudentIdWithFilters(
                studentId, null, startOfDay, endOfDay, null, null);

        int completedCount = (int) todayTasks.stream()
                .filter(t -> TaskStatus.COMPLETED.equals(t.getStatus()))
                .count();

        int totalCount = todayTasks.size();

        int pendingCount = totalCount - completedCount;

        int completionPercentage = totalCount == 0 ? 0
                : (int) Math.round((completedCount * 100.0) / totalCount);

        double totalScheduledHours = todayTasks.stream()
                .filter(t -> t.getEstimatedDurationMinutes() != null)
                .mapToInt(Task::getEstimatedDurationMinutes)
                .sum() / 60.0;

        totalScheduledHours = Math.round(totalScheduledHours * 10.0) / 10.0;

        DailySummaryResponse summary = DailySummaryResponse.builder()
                .completionPercentage(completionPercentage)
                .totalScheduledHours(totalScheduledHours)
                .completedCount(completedCount)
                .pendingCount(pendingCount)
                .build();
        log.debug("DAILY_SUMMARY | studentId={} | total={} | completed={} | pending={} | completion={}% | scheduledHours={}h",
                studentId, totalCount, completedCount, pendingCount, completionPercentage, totalScheduledHours);
        return summary;
    }
}
