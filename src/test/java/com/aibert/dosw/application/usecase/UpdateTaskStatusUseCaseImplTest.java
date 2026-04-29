package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    // ─── AC2 ──────────────────────────────────────────────────────────────────

    @Test
    void updateStatus_ShouldUpdateStatusAndReturn200() {
        // Arrange
        Task existing = Task.builder().id("t1").status(TaskStatus.TODO).build();
        Task saved    = Task.builder().id("t1").status(TaskStatus.IN_PROGRESS).build();

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(saved);

        // Act
        Task result = updateTaskStatusUseCase.updateStatus("t1", TaskStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepositoryPort).save(any(Task.class));
    }

    @Test
    void updateStatus_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        // Arrange
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class,
                () -> updateTaskStatusUseCase.updateStatus("missing", TaskStatus.IN_PROGRESS));
        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── AC3 ──────────────────────────────────────────────────────────────────

    @Test
    void updateStatus_WhenCompletedStatus_ShouldSetCompletedAt() {
        // Arrange
        Task existing = Task.builder().id("t2").status(TaskStatus.IN_PROGRESS).build();
        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Task result = updateTaskStatusUseCase.updateStatus("t2", TaskStatus.COMPLETED);

        // Assert
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt(), "completedAt debe registrarse automáticamente al completar");
    }

    @Test
    void updateStatus_WhenNotCompletedStatus_ShouldNotSetCompletedAt() {
        // Arrange
        Task existing = Task.builder().id("t3").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Task result = updateTaskStatusUseCase.updateStatus("t3", TaskStatus.IN_PROGRESS);

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getCompletedAt(), "completedAt no debe establecerse si el estado no es COMPLETED");
    }

    @Test
    void updateStatus_WhenRevertingFromCompleted_ShouldClearCompletedAt() {
        // Arrange — task was previously completed
        Task existing = Task.builder().id("t4").status(TaskStatus.COMPLETED)
                .completedAt(java.time.LocalDateTime.now().minusDays(1)).build();
        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Task result = updateTaskStatusUseCase.updateStatus("t4", TaskStatus.IN_PROGRESS);

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getCompletedAt(), "completedAt debe borrarse al revertir el estado");
    }
}
