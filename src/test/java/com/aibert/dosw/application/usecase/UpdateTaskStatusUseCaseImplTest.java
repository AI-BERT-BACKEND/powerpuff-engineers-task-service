package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskEventPort;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTaskStatusUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @Mock
    private TaskEventPort taskEventPort;

    @InjectMocks
    private UpdateTaskStatusUseCaseImpl updateTaskStatusUseCase;

    @Test
    void updateStatus_ShouldUpdateStatusAndReturn200() {
        Task existing = Task.builder().id("t1").studentId("S1").status(TaskStatus.TODO).build();
        Task saved    = Task.builder().id("t1").studentId("S1").status(TaskStatus.IN_PROGRESS).build();

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(saved);
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(saved));

        Task result = updateTaskStatusUseCase.updateStatus("t1", "S1", TaskStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepositoryPort).save(any(Task.class));
    }

    @Test
    void updateStatus_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> updateTaskStatusUseCase.updateStatus("missing", "S1", TaskStatus.IN_PROGRESS));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateStatus_WhenCompletedStatus_ShouldSetCompletedAt() {
        Task existing = Task.builder().id("t2").studentId("S1").status(TaskStatus.IN_PROGRESS).build();
        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of());

        Task result = updateTaskStatusUseCase.updateStatus("t2", "S1", TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt(), "completedAt debe registrarse automáticamente al completar");
    }

    @Test
    void updateStatus_WhenNotCompletedStatus_ShouldNotSetCompletedAt() {
        Task existing = Task.builder().id("t3").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of());

        Task result = updateTaskStatusUseCase.updateStatus("t3", "S1", TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getCompletedAt(), "completedAt no debe establecerse si el estado no es COMPLETED");
    }

    @Test
    void updateStatus_WhenRevertingFromCompleted_ShouldClearCompletedAt() {
        Task existing = Task.builder().id("t4").studentId("S1").status(TaskStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusDays(1)).build();
        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of());

        Task result = updateTaskStatusUseCase.updateStatus("t4", "S1", TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getCompletedAt(), "completedAt debe borrarse al revertir el estado");
    }

    // R42 — RN-01: ownership check
    @Test
    void updateStatus_WhenStudentIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task existing = Task.builder().id("t5").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t5")).thenReturn(Optional.of(existing));

        assertThrows(TaskForbiddenException.class,
                () -> updateTaskStatusUseCase.updateStatus("t5", "S_OTHER", TaskStatus.IN_PROGRESS));
        verify(taskRepositoryPort, never()).save(any());
    }

    // R42 — RN-04: priority recalculation after status change
    @Test
    void updateStatus_WhenOtherTaskHasUrgentDeadline_ShouldEscalatePriority() {
        Task target = Task.builder().id("t6").studentId("S2").status(TaskStatus.TODO).build();
        Task urgent = Task.builder().id("t7").studentId("S2").status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .deadline(LocalDateTime.now().plusHours(12))
                .build();

        when(taskRepositoryPort.findById("t6")).thenReturn(Optional.of(target));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepositoryPort.findByStudentId("S2")).thenReturn(List.of(urgent));

        updateTaskStatusUseCase.updateStatus("t6", "S2", TaskStatus.IN_PROGRESS);

        assertEquals(TaskPriority.HIGH, urgent.getPriority());
        verify(taskRepositoryPort).saveAll(anyList());
    }

    @Test
    void updateStatus_WhenNoUrgentTasksExist_ShouldNotCallSaveAll() {
        Task target = Task.builder().id("t8").studentId("S3").status(TaskStatus.TODO).build();
        Task notUrgent = Task.builder().id("t9").studentId("S3").status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .deadline(LocalDateTime.now().plusDays(10))
                .build();

        when(taskRepositoryPort.findById("t8")).thenReturn(Optional.of(target));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepositoryPort.findByStudentId("S3")).thenReturn(List.of(notUrgent));

        updateTaskStatusUseCase.updateStatus("t8", "S3", TaskStatus.IN_PROGRESS);

        verify(taskRepositoryPort, never()).saveAll(anyList());
    }
}
