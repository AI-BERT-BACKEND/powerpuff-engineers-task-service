package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
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
                .status(TaskStatus.TODO)
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Test Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(savedTask);

        Task result = createTaskUseCase.createTask(newTask);

        assertNotNull(result);
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals("some-uuid", result.getId());
        verify(taskRepositoryPort, times(1)).save(newTask);
    }

    @Test
    void createTask_WhenStatusIsAlreadySet_ShouldNotOverride() {
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

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepositoryPort, times(1)).save(newTask);
    }

    @Test
    void createTask_WhenPriorityIsNull_ShouldAssignMedium() {
        Task newTask = Task.builder()
                .title("No Priority Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .estimatedDurationMinutes(30)
                .deadline(LocalDateTime.now().plusDays(2))
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "No Priority Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }

    @Test
    void createTask_WhenSubjectNotFound_ShouldThrowSubjectNotFoundException() {
        Task task = Task.builder()
                .title("Task")
                .studentId("S123")
                .subjectId("UNKNOWN")
                .build();

        when(subjectValidationPort.exists("UNKNOWN")).thenReturn(false);

        assertThrows(SubjectNotFoundException.class, () -> createTaskUseCase.createTask(task));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void createTask_WhenDuplicateExists_ShouldThrowTaskConflictException() {
        Task task = Task.builder()
                .title("Dup Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Dup Task")).thenReturn(true);

        assertThrows(TaskConflictException.class, () -> createTaskUseCase.createTask(task));
        verify(taskRepositoryPort, never()).save(any());
    }

    // RN1 / RN2: priority escalation at creation time

    @Test
    void createTask_WhenDeadlineWithin24h_AndNullPriority_ShouldEscalateToHigh() {
        Task newTask = Task.builder()
                .title("Urgent Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .deadline(LocalDateTime.now().plusHours(10))
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Urgent Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.HIGH, result.getPriority());
    }

    @Test
    void createTask_WhenDeadlineWithin24h_AndLowPriority_ShouldEscalateToHigh() {
        Task newTask = Task.builder()
                .title("Urgent Low Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .priority(TaskPriority.LOW)
                .deadline(LocalDateTime.now().plusHours(6))
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Urgent Low Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.HIGH, result.getPriority());
    }

    @Test
    void createTask_WhenDeadlineWithin24h_AndCriticalPriority_ShouldNotDowngrade() {
        Task newTask = Task.builder()
                .title("Critical Urgent Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .priority(TaskPriority.CRITICAL)
                .deadline(LocalDateTime.now().plusHours(3))
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Critical Urgent Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.CRITICAL, result.getPriority());
    }

    @Test
    void createTask_WhenDeadlineBeyond24h_AndNullPriority_ShouldAssignMedium() {
        Task newTask = Task.builder()
                .title("Non-urgent Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .deadline(LocalDateTime.now().plusDays(3))
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Non-urgent Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }

    @Test
    void createTask_WhenNullDeadline_AndNullPriority_ShouldAssignMedium() {
        Task newTask = Task.builder()
                .title("No Deadline Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .build();

        when(subjectValidationPort.exists("MATH-101")).thenReturn(true);
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "No Deadline Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }
}
