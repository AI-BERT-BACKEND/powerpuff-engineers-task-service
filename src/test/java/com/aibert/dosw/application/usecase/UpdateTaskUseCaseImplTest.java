package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
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
class UpdateTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private UpdateTaskUseCaseImpl updateTaskUseCase;

    @Test
    void updateTask_ShouldUpdateFieldsAndReturnTask() {
        Task existing = Task.builder()
                .id("t1").studentId("S1").title("Old title")
                .status(TaskStatus.TODO).priority(TaskPriority.LOW).build();
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("New title").priority(TaskPriority.HIGH).build();

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateTaskUseCase.updateTask("t1", "S1", request);

        assertEquals("New title", result.getTitle());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        verify(taskRepositoryPort).save(any(Task.class));
    }

    @Test
    void updateTask_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> updateTaskUseCase.updateTask("missing", "S1", UpdateTaskRequest.builder().build()));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateTask_WhenUserIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task existing = Task.builder().id("t2").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(existing));

        assertThrows(TaskForbiddenException.class,
                () -> updateTaskUseCase.updateTask("t2", "OTHER", UpdateTaskRequest.builder().build()));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateTask_WhenTaskIsCompleted_ShouldThrowTaskEditNotAllowedException() {
        Task existing = Task.builder().id("t3").studentId("S1").status(TaskStatus.COMPLETED).build();
        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(existing));

        assertThrows(com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException.class,
                () -> updateTaskUseCase.updateTask("t3", "S1", UpdateTaskRequest.builder().title("x").build()));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateTask_ShouldUpdateTaskTypeAndDeadline() {
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(5);
        Task existing = Task.builder().id("t4").studentId("S1").status(TaskStatus.TODO)
                .taskType(TaskType.TAREA).build();
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .taskType(TaskType.EXAMEN).deadline(newDeadline).build();

        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = updateTaskUseCase.updateTask("t4", "S1", request);

        assertEquals(TaskType.EXAMEN, result.getTaskType());
        assertEquals(newDeadline, result.getDeadline());
    }
}
