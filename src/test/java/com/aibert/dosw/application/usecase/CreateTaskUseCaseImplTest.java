package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @Mock
    private SubjectValidationPort subjectValidationPort;

    @InjectMocks
    private CreateTaskUseCaseImpl createTaskUseCase;

    @Test
    void createTask_ShouldAssignTodoStatusAndSave() {
        // Arrange
        Task newTask = Task.builder()
                .title("Test Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .priority(TaskPriority.HIGH)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(1))
                .build();

        Task savedTask = Task.builder()
                .id("some-uuid")
                .title("Test Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .priority(TaskPriority.HIGH)
                .estimatedDurationMinutes(60)
                .deadline(newTask.getDeadline())
                .status(TaskStatus.TODO) // This is what the use case sets
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Test Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = createTaskUseCase.createTask(newTask);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals("some-uuid", result.getId());
        verify(taskRepositoryPort, times(1)).save(newTask);
    }

    @Test
    void createTask_WhenStatusIsAlreadySet_ShouldNotOverride() {
        // Arrange
        Task newTask = Task.builder()
                .title("Test Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Test Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(newTask);

        // Act
        Task result = createTaskUseCase.createTask(newTask);

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepositoryPort, times(1)).save(newTask);
    }
}
