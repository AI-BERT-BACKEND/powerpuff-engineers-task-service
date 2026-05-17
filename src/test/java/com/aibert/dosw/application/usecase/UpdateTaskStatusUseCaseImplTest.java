package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTaskStatusUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private UpdateTaskStatusUseCaseImpl updateTaskStatusUseCase;

    @Test
    void updateStatus_ShouldUpdateStatusAndReturn200() {
        Task existing = Task.builder().id("t1").studentId("S1").status(TaskStatus.TODO).build();
        Task saved    = Task.builder().id("t1").studentId("S1").status(TaskStatus.IN_PROGRESS).build();

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(saved);

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

        Task result = updateTaskStatusUseCase.updateStatus("t2", "S1", TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt(), "completedAt debe registrarse automáticamente al completar");
    }

    @Test
    void updateStatus_WhenNotCompletedStatus_ShouldNotSetCompletedAt() {
        Task existing = Task.builder().id("t3").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateTaskStatusUseCase.updateStatus("t3", "S1", TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getCompletedAt(), "completedAt no debe establecerse si el estado no es COMPLETED");
    }

    @Test
    void updateStatus_WhenRevertingFromCompleted_ShouldClearCompletedAt() {
        Task existing = Task.builder().id("t4").studentId("S1").status(TaskStatus.COMPLETED)
                .completedAt(java.time.LocalDateTime.now().minusDays(1)).build();
        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateTaskStatusUseCase.updateStatus("t4", "S1", TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getCompletedAt(), "completedAt debe borrarse al revertir el estado");
    }

    @Test
    void updateStatus_WhenUserIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task existing = Task.builder().id("t5").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t5")).thenReturn(Optional.of(existing));

        assertThrows(com.aibert.dosw.domain.exceptions.TaskForbiddenException.class,
                () -> updateTaskStatusUseCase.updateStatus("t5", "OTHER_USER", TaskStatus.IN_PROGRESS));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateStatus_WhenPausingFromInProgress_ShouldAllowTransition() {
        Task existing = Task.builder().id("t6").studentId("S1").status(TaskStatus.IN_PROGRESS).build();
        when(taskRepositoryPort.findById("t6")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateTaskStatusUseCase.updateStatus("t6", "S1", TaskStatus.PAUSED);

        assertEquals(TaskStatus.PAUSED, result.getStatus());
        verify(taskRepositoryPort).save(any(Task.class));
    }

    @Test
    void updateStatus_WhenPausingFromTodo_ShouldThrowIllegalStateException() {
        Task existing = Task.builder().id("t7").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t7")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> updateTaskStatusUseCase.updateStatus("t7", "S1", TaskStatus.PAUSED));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateStatus_WhenResumingFromPaused_ShouldAllowTransitionToInProgress() {
        Task existing = Task.builder().id("t8").studentId("S1").status(TaskStatus.PAUSED).build();
        when(taskRepositoryPort.findById("t8")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateTaskStatusUseCase.updateStatus("t8", "S1", TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepositoryPort).save(any(Task.class));
    }
}
