package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.ConflictResponse;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCalendarConflictsUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private GetCalendarConflictsUseCaseImpl getCalendarConflictsUseCase;

    private Task buildTask(String id, LocalDateTime start, Integer durationMinutes) {
        return Task.builder()
                .id(id)
                .studentId("S1")
                .title("Task " + id)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .scheduledDate(start)
                .estimatedDurationMinutes(durationMinutes)
                .build();
    }

    // ─── Happy path: no conflicts ────────────────────────────────────────────────

    @Test
    void getConflicts_WhenNoOverlap_ShouldReturnEmptyList() {
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("t1", base, 60);             // 09:00–10:00
        Task b = buildTask("t2", base.plusMinutes(60), 60); // 10:00–11:00  — adjacent, no overlap

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b));

        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", null, null);

        assertTrue(result.isEmpty());
    }

    // ─── Partial overlap ─────────────────────────────────────────────────────────

    @Test
    void getConflicts_WhenPartialOverlap_ShouldReturnOneConflict() {
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("t1", base, 120);                  // 09:00–11:00
        Task b = buildTask("t2", base.plusMinutes(60), 120);  // 10:00–12:00 — overlaps 10:00–11:00

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b));

        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", null, null);

        assertEquals(1, result.size());
        ConflictResponse c = result.get(0);
        assertEquals("t1", c.getTaskAId());
        assertEquals("t2", c.getTaskBId());
    }

    // ─── Total overlap (one task completely inside another) ──────────────────────

    @Test
    void getConflicts_WhenTotalOverlap_ShouldReturnOneConflict() {
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("t1", base, 120);           // 09:00–11:00
        Task b = buildTask("t2", base.plusMinutes(15), 30); // 09:15–09:45 — fully inside A

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b));

        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", null, null);

        assertEquals(1, result.size());
    }

    // ─── Multiple conflicts ──────────────────────────────────────────────────────

    @Test
    void getConflicts_WhenMultipleOverlaps_ShouldReturnAllPairs() {
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("t1", base, 120);                  // 09:00–11:00
        Task b = buildTask("t2", base.plusMinutes(30), 120);  // 09:30–11:30 — overlaps A
        Task c = buildTask("t3", base.plusMinutes(60), 120);  // 10:00–12:00 — overlaps A and B

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b, c));

        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", null, null);

        // pairs: (A,B), (A,C), (B,C) = 3 conflicts
        assertEquals(3, result.size());
    }

    // ─── Date range filter ───────────────────────────────────────────────────────

    @Test
    void getConflicts_WhenDateRangeExcludesConflict_ShouldReturnEmpty() {
        LocalDateTime base = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("t1", base, 120);
        Task b = buildTask("t2", base.plusMinutes(60), 120);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b));

        // request only today's range — tasks are 5 days out
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", today,
                today.plusDays(1));

        assertTrue(result.isEmpty());
    }

    @Test
    void getConflicts_WhenDateRangeIncludesConflict_ShouldReturnConflict() {
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("t1", base, 120);
        Task b = buildTask("t2", base.plusMinutes(60), 120);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b));

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(3);
        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", start, end);

        assertEquals(1, result.size());
    }

    // ─── Tasks without scheduledDate are ignored ─────────────────────────────────

    @Test
    void getConflicts_WhenTaskHasNoScheduledDate_ShouldBeIgnored() {
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task scheduled   = buildTask("t1", base, 120);
        Task unscheduled = buildTask("t2", null, 60);   // no scheduledDate

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(scheduled, unscheduled));

        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", null, null);

        assertTrue(result.isEmpty());
    }

    // ─── ConflictResponse fields are populated correctly ─────────────────────────

    @Test
    void getConflicts_ResponseFieldsShouldBeCorrect() {
        LocalDateTime base = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task a = buildTask("tA", base, 120);
        Task b = buildTask("tB", base.plusMinutes(60), 60);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(a, b));

        List<ConflictResponse> result = getCalendarConflictsUseCase.getConflicts("S1", null, null);

        assertEquals(1, result.size());
        ConflictResponse cr = result.get(0);
        assertEquals(base, cr.getTaskAStart());
        assertEquals(base.plusMinutes(120), cr.getTaskAEnd());
        assertEquals(base.plusMinutes(60), cr.getTaskBStart());
        assertEquals(base.plusMinutes(120), cr.getTaskBEnd());
        assertEquals("Task tA", cr.getTaskATitle());
        assertEquals("Task tB", cr.getTaskBTitle());
    }
}
