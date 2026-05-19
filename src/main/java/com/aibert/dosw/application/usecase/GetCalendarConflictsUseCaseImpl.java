package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.ConflictResponse;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.GetCalendarConflictsUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring service implementing the {@link GetCalendarConflictsUseCase} input port (R17).
 * Scans all scheduled tasks of a student within an optional date range and returns
 * every overlapping pair as a {@link ConflictResponse}.
 */
@Service
@RequiredArgsConstructor
public class GetCalendarConflictsUseCaseImpl implements GetCalendarConflictsUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Load all tasks for the student that have a {@code scheduledDate}.</li>
     *   <li>If {@code startDate} / {@code endDate} are provided, filter by whether the
     *       task's {@code scheduledDate} falls within the range.</li>
     *   <li>For each unique pair (A, B), check whether their time blocks overlap.</li>
     *   <li>Return the list of overlapping pairs; empty if no conflicts exist.</li>
     * </ol>
     */
    @Override
    public List<ConflictResponse> getConflicts(String studentId,
                                                LocalDateTime startDate,
                                                LocalDateTime endDate) {
        List<Task> scheduled = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> t.getScheduledDate() != null)
                .filter(t -> startDate == null || !t.getScheduledDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getScheduledDate().isAfter(endDate))
                .toList();

        List<ConflictResponse> conflicts = new ArrayList<>();

        for (int i = 0; i < scheduled.size(); i++) {
            for (int j = i + 1; j < scheduled.size(); j++) {
                Task a = scheduled.get(i);
                Task b = scheduled.get(j);

                LocalDateTime aStart = a.getScheduledDate();
                LocalDateTime aEnd = aStart.plusMinutes(
                        a.getEstimatedDurationMinutes() != null ? a.getEstimatedDurationMinutes() : 0);

                LocalDateTime bStart = b.getScheduledDate();
                LocalDateTime bEnd = bStart.plusMinutes(
                        b.getEstimatedDurationMinutes() != null ? b.getEstimatedDurationMinutes() : 0);

                boolean overlaps = aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
                if (overlaps) {
                    conflicts.add(ConflictResponse.builder()
                            .taskAId(a.getId())
                            .taskATitle(a.getTitle())
                            .taskAStart(aStart)
                            .taskAEnd(aEnd)
                            .taskBId(b.getId())
                            .taskBTitle(b.getTitle())
                            .taskBStart(bStart)
                            .taskBEnd(bEnd)
                            .build());
                }
            }
        }

        return conflicts;
    }
}
