package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescheduleTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private RescheduleTaskUseCaseImpl rescheduleTaskUseCase;

    private Task buildTask(String id, String studentId, LocalDateTime deadline,
                            LocalDateTime scheduledDate, Integer durationMinutes) {
        return Task.builder()
                .id(id)
                .studentId(studentId)
                .title("Task " + id)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .deadline(deadline)
                .scheduledDate(scheduledDate)
                .estimatedDurationMinutes(durationMinutes)
                .build();
    }

    // ─── Happy path ─────────────────────────────────────────────────────────────

    @Test
    void rescheduleTask_ValidNewDate_ShouldPersistAndReturn() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);
        LocalDateTime newDate  = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Task task = buildTask("t1", "S1", deadline, null, 60);

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = rescheduleTaskUseCase.rescheduleTask("t1", "S1", newDate);

        assertEquals(newDate, result.getScheduledDate());
        verify(taskRepositoryPort).save(any(Task.class));
    }

    @Test
    void rescheduleTask_ScheduledDateEqualsDeadline_ShouldSucceed() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        Task task = buildTask("t2", "S1", deadline, null, 30);

        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // exactly on the deadline should be allowed
        Task result = rescheduleTaskUseCase.rescheduleTask("t2", "S1", deadline);

        assertEquals(deadline, result.getScheduledDate());
    }

    // ─── CA2: scheduledDate after deadline → 409 ────────────────────────────────

    @Test
    void rescheduleTask_WhenNewDateExceedsDeadline_ShouldThrow409() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(2);
        LocalDateTime tooLate  = deadline.plusDays(1);
        Task task = buildTask("t3", "S1", deadline, null, 60);

        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(task));

        TaskConflictException ex = assertThrows(TaskConflictException.class,
                () -> rescheduleTaskUseCase.rescheduleTask("t3", "S1", tooLate));

        assertTrue(ex.getMessage().contains("deadline"));
        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── CA2: scheduledDate overlaps another task → 409 ─────────────────────────

    @Test
    void rescheduleTask_WhenNewDateOverlapsAnotherTask_ShouldThrow409() {
        LocalDateTime deadline  = LocalDateTime.now().plusDays(5);
        LocalDateTime otherStart = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

        Task taskToMove = buildTask("t4", "S1", deadline, null, 60);
        Task blocker    = buildTask("t5", "S1", deadline, otherStart, 120); // occupies 09:00–11:00

        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(taskToMove));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(taskToMove, blocker));

        // trying to schedule t4 at 09:30 — overlaps with blocker (09:00–11:00)
        LocalDateTime conflicting = otherStart.plusMinutes(30);

        TaskConflictException ex = assertThrows(TaskConflictException.class,
                () -> rescheduleTaskUseCase.rescheduleTask("t4", "S1", conflicting));

        assertTrue(ex.getMessage().contains("solapa"));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void rescheduleTask_AdjacentBlocks_ShouldNotConflict() {
        LocalDateTime deadline  = LocalDateTime.now().plusDays(5);
        LocalDateTime existingStart = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

        Task taskToMove = buildTask("t6", "S1", deadline, null, 60);
        Task existing   = buildTask("t7", "S1", deadline, existingStart, 60); // occupies 09:00–10:00

        when(taskRepositoryPort.findById("t6")).thenReturn(Optional.of(taskToMove));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(taskToMove, existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // scheduling t6 at 10:00 — starts exactly when existing ends → no overlap
        LocalDateTime adjacent = existingStart.plusMinutes(60);

        assertDoesNotThrow(() -> rescheduleTaskUseCase.rescheduleTask("t6", "S1", adjacent));
        verify(taskRepositoryPort).save(any(Task.class));
    }

    // ─── RN-01: ownership ────────────────────────────────────────────────────────

    @Test
    void rescheduleTask_WhenStudentIsNotOwner_ShouldThrowForbidden() {
        Task task = buildTask("t8", "owner", LocalDateTime.now().plusDays(5), null, 60);

        when(taskRepositoryPort.findById("t8")).thenReturn(Optional.of(task));

        assertThrows(TaskForbiddenException.class,
                () -> rescheduleTaskUseCase.rescheduleTask("t8", "other-student",
                        LocalDateTime.now().plusDays(1)));

        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── Task not found ──────────────────────────────────────────────────────────

    @Test
    void rescheduleTask_WhenTaskNotFound_ShouldThrowNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> rescheduleTaskUseCase.rescheduleTask("missing", "S1",
                        LocalDateTime.now().plusDays(1)));

        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── Null deadline edge case ─────────────────────────────────────────────────

    @Test
    void rescheduleTask_WhenDeadlineIsNull_ShouldSkipDeadlineCheck() {
        Task task = buildTask("t9", "S1", null, null, 30);
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);

        when(taskRepositoryPort.findById("t9")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> rescheduleTaskUseCase.rescheduleTask("t9", "S1", newDate));
    }
}
