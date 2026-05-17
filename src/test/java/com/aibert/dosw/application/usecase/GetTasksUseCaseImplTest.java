package com.aibert.dosw.application.usecase;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTasksUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private GetTasksUseCaseImpl getTasksUseCase;

    @Test
    void getTasksByStudentId_ShouldReturnTasksFromRepository() {
        List<Task> tasks = List.of(
                Task.builder().id("1").studentId("S1").title("Task A").status(TaskStatus.TODO).build(),
                Task.builder().id("2").studentId("S1").title("Task B").status(TaskStatus.IN_PROGRESS).build()
        );
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(tasks);

        List<Task> result = getTasksUseCase.getTasksByStudentId("S1");

        assertEquals(2, result.size());
        verify(taskRepositoryPort).findByStudentId("S1");
    }

    @Test
    void getTasksByStudentId_WhenNoTasks_ShouldReturnEmptyList() {
        when(taskRepositoryPort.findByStudentId("S99")).thenReturn(List.of());

        List<Task> result = getTasksUseCase.getTasksByStudentId("S99");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenExists() {
        Task task = Task.builder().id("T1").studentId("S1").title("Exam").status(TaskStatus.TODO).build();
        when(taskRepositoryPort.findById("T1")).thenReturn(Optional.of(task));

        Task result = getTasksUseCase.getTaskById("T1");

        assertEquals("T1", result.getId());
        verify(taskRepositoryPort).findById("T1");
    }

    @Test
    void getTaskById_ShouldThrowTaskNotFoundException_WhenNotExists() {
        when(taskRepositoryPort.findById("MISSING")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> getTasksUseCase.getTaskById("MISSING"));
    }
}
