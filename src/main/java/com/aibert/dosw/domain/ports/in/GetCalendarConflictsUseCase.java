package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.ConflictResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Input port for detecting time-block overlaps between scheduled tasks (R17).
 * Used by the frontend to highlight conflicting blocks with a red border.
 */
public interface GetCalendarConflictsUseCase {

    /**
     * Returns all pairs of tasks whose scheduled time blocks overlap within the given date range.
     *
     * <p>Two tasks conflict when:</p>
     * <pre>
     *   taskA.scheduledDate &lt; taskB.scheduledDate + taskB.duration
     *   AND
     *   taskA.scheduledDate + taskA.duration &gt; taskB.scheduledDate
     * </pre>
     *
     * @param studentId the student whose tasks are inspected
     * @param startDate optional lower bound for filtering by {@code scheduledDate} (inclusive)
     * @param endDate   optional upper bound for filtering by {@code scheduledDate} (inclusive)
     * @return list of {@link ConflictResponse} objects, one per overlapping pair; empty if no conflicts
     */
    List<ConflictResponse> getConflicts(String studentId, LocalDateTime startDate, LocalDateTime endDate);
}
