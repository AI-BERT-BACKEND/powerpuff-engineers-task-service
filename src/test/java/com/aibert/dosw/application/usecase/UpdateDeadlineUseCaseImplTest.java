package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDeadlineUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private UpdateDeadlineUseCaseImpl updateDeadlineUseCase;

    private Task buildTask(String id, String studentId, TaskPriority priority, TaskStatus status) {
        return Task.builder()
                .id(id)
                .studentId(studentId)
                .title("Task " + id)
                .priority(priority)
                .status(status)
                .deadline(LocalDateTime.now().plusDays(5))
                .build();
    }

    // ─── Happy path ─────────────────────────────────────────────────────────────

    @Test
    void updateDeadline_ShouldPersistNewDeadlineAndReturn() {
        Task task = buildTask("t1", "S1", TaskPriority.MEDIUM, TaskStatus.TODO);
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(3);

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateDeadlineUseCase.updateDeadline("t1", "S1", newDeadline);

        assertEquals(newDeadline, result.getDeadline());
        verify(taskRepositoryPort).save(task);
    }

    // ─── RN-02: priority escalation when new deadline is within 24 h ────────────

    @Test
    void updateDeadline_WhenNewDeadlineWithin24h_AndMediumPriority_ShouldEscalateToHigh() {
        Task task = buildTask("t2", "S1", TaskPriority.MEDIUM, TaskStatus.TODO);
        LocalDateTime urgentDeadline = LocalDateTime.now().plusHours(12);

        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateDeadlineUseCase.updateDeadline("t2", "S1", urgentDeadline);

        assertEquals(TaskPriority.HIGH, result.getPriority());
    }

    @Test
    void updateDeadline_WhenNewDeadlineWithin24h_AndAlreadyHigh_ShouldNotChange() {
        Task task = buildTask("t3", "S1", TaskPriority.HIGH, TaskStatus.TODO);
        LocalDateTime urgentDeadline = LocalDateTime.now().plusHours(6);

        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateDeadlineUseCase.updateDeadline("t3", "S1", urgentDeadline);

        assertEquals(TaskPriority.HIGH, result.getPriority());
    }

    @Test
    void updateDeadline_WhenNewDeadlineWithin24h_AndCritical_ShouldNotDowngrade() {
        Task task = buildTask("t4", "S1", TaskPriority.CRITICAL, TaskStatus.IN_PROGRESS);
        LocalDateTime urgentDeadline = LocalDateTime.now().plusHours(3);

        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateDeadlineUseCase.updateDeadline("t4", "S1", urgentDeadline);

        assertEquals(TaskPriority.CRITICAL, result.getPriority());
    }

    @Test
    void updateDeadline_WhenNewDeadlineBeyond24h_ShouldNotEscalatePriority() {
        Task task = buildTask("t5", "S1", TaskPriority.LOW, TaskStatus.TODO);
        LocalDateTime farDeadline = LocalDateTime.now().plusDays(7);

        when(taskRepositoryPort.findById("t5")).thenReturn(Optional.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateDeadlineUseCase.updateDeadline("t5", "S1", farDeadline);

        assertEquals(TaskPriority.LOW, result.getPriority());
    }

    // ─── Error cases ─────────────────────────────────────────────────────────────

    @Test
    void updateDeadline_WhenTaskNotFound_ShouldThrowNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> updateDeadlineUseCase.updateDeadline("missing", "S1",
                        LocalDateTime.now().plusDays(1)));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateDeadline_WhenStudentIsNotOwner_ShouldThrowForbiddenException() {
        Task task = buildTask("t6", "owner", TaskPriority.MEDIUM, TaskStatus.TODO);
        when(taskRepositoryPort.findById("t6")).thenReturn(Optional.of(task));

        assertThrows(TaskForbiddenException.class,
                () -> updateDeadlineUseCase.updateDeadline("t6", "other-student",
                        LocalDateTime.now().plusDays(1)));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateDeadline_WhenTaskIsCompleted_ShouldThrowEditNotAllowedException() {
        Task task = buildTask("t7", "S1", TaskPriority.MEDIUM, TaskStatus.COMPLETED);
        when(taskRepositoryPort.findById("t7")).thenReturn(Optional.of(task));

        assertThrows(TaskEditNotAllowedException.class,
                () -> updateDeadlineUseCase.updateDeadline("t7", "S1",
                        LocalDateTime.now().plusDays(1)));
        verify(taskRepositoryPort, never()).save(any());
    }
}
