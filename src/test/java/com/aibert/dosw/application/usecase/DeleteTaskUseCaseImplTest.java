package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private DeleteTaskUseCaseImpl deleteTaskUseCase;

    @Test
    void deleteTask_ShouldCallDeleteById() {
        Task existing = Task.builder().id("t1").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(existing));

        deleteTaskUseCase.deleteTask("t1", "S1");

        verify(taskRepositoryPort).deleteById("t1");
    }

    @Test
    void deleteTask_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> deleteTaskUseCase.deleteTask("missing", "S1"));
        verify(taskRepositoryPort, never()).deleteById(any());
    }

    @Test
    void deleteTask_WhenUserIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task existing = Task.builder().id("t2").studentId("S1").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(existing));

        assertThrows(TaskForbiddenException.class,
                () -> deleteTaskUseCase.deleteTask("t2", "OTHER_USER"));
        verify(taskRepositoryPort, never()).deleteById(any());
    }
}
