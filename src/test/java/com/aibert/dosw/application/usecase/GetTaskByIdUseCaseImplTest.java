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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTaskByIdUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private GetTaskByIdUseCaseImpl getTaskByIdUseCase;

    @Test
    void getTaskById_WhenExists_ShouldReturnTask() {
        Task task = Task.builder()
                .id("task-123")
                .studentId("S1")
                .title("Estudiar para examen")
                .status(TaskStatus.TODO)
                .build();

        when(taskRepositoryPort.findById("task-123")).thenReturn(Optional.of(task));

        Task result = getTaskByIdUseCase.getTaskById("task-123");

        assertNotNull(result);
        assertEquals("task-123", result.getId());
        assertEquals("S1", result.getStudentId());
        assertEquals("Estudiar para examen", result.getTitle());
        verify(taskRepositoryPort).findById("task-123");
    }

    @Test
    void getTaskById_WhenNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> getTaskByIdUseCase.getTaskById("unknown-id"));

        verify(taskRepositoryPort).findById("unknown-id");
    }
}
