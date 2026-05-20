package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;
import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.SubjectNotInActiveSemesterException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.model.NotificationSeverity;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.domain.ports.out.TaskNotificationPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @Mock
    private SubjectValidationPort subjectValidationPort;

    @Mock
    private TaskNotificationPort taskNotificationPort;

    @InjectMocks
    private CreateTaskUseCaseImpl createTaskUseCase;

    // shared stubs for the subject/semester happy-path
    private void stubSubjectValid(String subjectId, String studentId) {
        when(subjectValidationPort.exists(subjectId, studentId)).thenReturn(true);
        when(subjectValidationPort.isInActiveSemester(subjectId, studentId)).thenReturn(true);
        when(taskRepositoryPort.findByStudentId(studentId)).thenReturn(List.of());
    }

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

        stubSubjectValid("MATH-101", "S123");
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

        stubSubjectValid("MATH-101", "S123");
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

        stubSubjectValid("MATH-101", "S123");
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

        when(subjectValidationPort.exists("UNKNOWN", "S123")).thenReturn(false);

        assertThrows(SubjectNotFoundException.class, () -> createTaskUseCase.createTask(task));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void createTask_WhenSubjectNotInActiveSemester_ShouldThrowSubjectNotInActiveSemesterException() {
        Task task = Task.builder()
                .title("Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .build();

        when(subjectValidationPort.exists("MATH-101", "S123")).thenReturn(true);
        when(subjectValidationPort.isInActiveSemester("MATH-101", "S123")).thenReturn(false);

        assertThrows(SubjectNotInActiveSemesterException.class, () -> createTaskUseCase.createTask(task));
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void createTask_WhenDuplicateExists_ShouldThrowTaskConflictException() {
        Task task = Task.builder()
                .title("Dup Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .build();

        when(subjectValidationPort.exists("MATH-101", "S123")).thenReturn(true);
        when(subjectValidationPort.isInActiveSemester("MATH-101", "S123")).thenReturn(true);
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

        stubSubjectValid("MATH-101", "S123");
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

        stubSubjectValid("MATH-101", "S123");
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

        stubSubjectValid("MATH-101", "S123");
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

        stubSubjectValid("MATH-101", "S123");
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

        stubSubjectValid("MATH-101", "S123");
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "No Deadline Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }

    @Test
    void createTask_ShouldRecalculateOtherActiveTasks() {
        Task urgentExisting = Task.builder()
                .id("existing-1")
                .studentId("S123")
                .subjectId("MATH-101")
                .title("Existing urgent")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .deadline(LocalDateTime.now().plusHours(5))
                .build();

        Task newTask = Task.builder()
                .title("New Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .deadline(LocalDateTime.now().plusDays(3))
                .build();

        Task savedNew = Task.builder().id("new-uuid").studentId("S123").title("New Task").build();

        stubSubjectValid("MATH-101", "S123");
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "New Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(savedNew);
        // findByStudentId is called after save for recalculation
        when(taskRepositoryPort.findByStudentId("S123")).thenReturn(List.of(urgentExisting));

        createTaskUseCase.createTask(newTask);

        // existing task with 5h deadline should have been escalated to HIGH
        assertEquals(TaskPriority.HIGH, urgentExisting.getPriority());
        verify(taskRepositoryPort).saveAll(anyList());
    }

    @Test
    void createTask_WhenPriorityLow_ShouldPublishNotificationWithLowSeverity() {
        Task newTask = Task.builder()
                .title("Low Priority Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .priority(TaskPriority.LOW)
                .deadline(LocalDateTime.now().plusDays(5))
                .build();

        stubSubjectValid("MATH-101", "S123");
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "Low Priority Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = createTaskUseCase.createTask(newTask);

        assertEquals(TaskPriority.LOW, result.getPriority());
        verify(taskNotificationPort).publish(argThat(e -> e.severity() == NotificationSeverity.LOW));
    }

    @Test
    void createTask_WhenActiveTaskHasNullDeadline_ShouldSkipEscalation() {
        Task activeWithNullDeadline = Task.builder()
                .id("existing-null-deadline")
                .studentId("S123")
                .subjectId("MATH-101")
                .title("No deadline task")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .deadline(null)
                .build();

        Task newTask = Task.builder()
                .title("New Task")
                .studentId("S123")
                .subjectId("MATH-101")
                .deadline(LocalDateTime.now().plusDays(3))
                .build();

        Task savedNew = Task.builder().id("new-uuid").studentId("S123").title("New Task").build();

        stubSubjectValid("MATH-101", "S123");
        when(taskRepositoryPort.existsDuplicate("S123", "MATH-101", "New Task")).thenReturn(false);
        when(taskRepositoryPort.save(any(Task.class))).thenReturn(savedNew);
        when(taskRepositoryPort.findByStudentId("S123")).thenReturn(List.of(activeWithNullDeadline));

        createTaskUseCase.createTask(newTask);

        // task with null deadline should keep its original LOW priority
        assertEquals(TaskPriority.LOW, activeWithNullDeadline.getPriority());
        verify(taskRepositoryPort, never()).saveAll(anyList());
    }
}
