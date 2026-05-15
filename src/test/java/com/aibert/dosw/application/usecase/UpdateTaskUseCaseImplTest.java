package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException;
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
import java.util.List;
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

    private Task buildTask(String id, String studentId, TaskStatus status, TaskPriority priority,
                           LocalDateTime deadline) {
        return Task.builder()
                .id(id)
                .studentId(studentId)
                .title("Original title")
                .subjectId("sub-1")
                .taskType(TaskType.TAREA)
                .estimatedDurationMinutes(60)
                .deadline(deadline)
                .priority(priority)
                .status(status)
                .build();
    }

    // ─── Happy path ─────────────────────────────────────────────────────────────

    @Test
    void updateTask_ShouldUpdateAllowedFields_WhenOwnerAndNotCompleted() {
        LocalDateTime originalDeadline = LocalDateTime.now().plusDays(10);
        Task existing = buildTask("t1", "student-1", TaskStatus.TODO, TaskPriority.MEDIUM, originalDeadline);

        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("New title")
                .taskType(TaskType.PROYECTO)
                .estimatedDurationMinutes(120)
                .build();

        Task result = updateTaskUseCase.updateTask("t1", "student-1", request);

        assertEquals("New title", result.getTitle());
        assertEquals(TaskType.PROYECTO, result.getTaskType());
        assertEquals(120, result.getEstimatedDurationMinutes());
        // Unchanged fields should remain
        assertEquals("sub-1", result.getSubjectId());
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        verify(taskRepositoryPort).save(any(Task.class));
    }

    @Test
    void updateTask_WhenNullFields_ShouldLeaveOriginalValues() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);
        Task existing = buildTask("t2", "student-1", TaskStatus.IN_PROGRESS, TaskPriority.LOW, deadline);

        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = UpdateTaskRequest.builder().build(); // all null

        Task result = updateTaskUseCase.updateTask("t2", "student-1", request);

        assertEquals("Original title", result.getTitle());
        assertEquals(TaskPriority.LOW, result.getPriority());
        assertEquals(deadline, result.getDeadline());
    }

    // ─── RN-01: ownership ────────────────────────────────────────────────────────

    @Test
    void updateTask_WhenStudentIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task existing = buildTask("t3", "owner-student", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDateTime.now().plusDays(5));

        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(existing));

        assertThrows(TaskForbiddenException.class,
                () -> updateTaskUseCase.updateTask("t3", "other-student", UpdateTaskRequest.builder().build()));

        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── RN-02: completed lock ────────────────────────────────────────────────────

    @Test
    void updateTask_WhenTaskIsCompleted_ShouldThrowTaskEditNotAllowedException() {
        Task existing = buildTask("t4", "student-1", TaskStatus.COMPLETED, TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1));

        when(taskRepositoryPort.findById("t4")).thenReturn(Optional.of(existing));

        assertThrows(TaskEditNotAllowedException.class,
                () -> updateTaskUseCase.updateTask("t4", "student-1", UpdateTaskRequest.builder().build()));

        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── RN-03: deadline change triggers priority escalation ─────────────────────

    @Test
    void updateTask_WhenDeadlineChangedToWithin24h_ShouldEscalatePriorityToHigh() {
        LocalDateTime urgentDeadline = LocalDateTime.now().plusHours(10);
        Task existing = buildTask("t5", "student-1", TaskStatus.TODO, TaskPriority.LOW,
                LocalDateTime.now().plusDays(10));

        when(taskRepositoryPort.findById("t5")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .deadline(urgentDeadline)
                .build();

        Task result = updateTaskUseCase.updateTask("t5", "student-1", request);

        assertEquals(TaskPriority.HIGH, result.getPriority(),
                "Priority should be escalated to HIGH when deadline is within 24 hours");
        assertEquals(urgentDeadline, result.getDeadline());
    }

    @Test
    void updateTask_WhenDeadlineChangedButNotUrgent_ShouldNotEscalatePriority() {
        Task existing = buildTask("t6", "student-1", TaskStatus.TODO, TaskPriority.LOW,
                LocalDateTime.now().plusDays(3));

        when(taskRepositoryPort.findById("t6")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime farDeadline = LocalDateTime.now().plusDays(10);
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .deadline(farDeadline)
                .build();

        Task result = updateTaskUseCase.updateTask("t6", "student-1", request);

        assertEquals(TaskPriority.LOW, result.getPriority(),
                "Priority should remain LOW when new deadline is more than 24 hours away");
    }

    @Test
    void updateTask_WhenDeadlineChangedButAlreadyCritical_ShouldNotDowngradePriority() {
        LocalDateTime urgentDeadline = LocalDateTime.now().plusHours(5);
        Task existing = buildTask("t7", "student-1", TaskStatus.TODO, TaskPriority.CRITICAL,
                LocalDateTime.now().plusDays(5));

        when(taskRepositoryPort.findById("t7")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .deadline(urgentDeadline)
                .build();

        Task result = updateTaskUseCase.updateTask("t7", "student-1", request);

        assertEquals(TaskPriority.CRITICAL, result.getPriority(),
                "CRITICAL priority should not be overwritten by escalation logic");
    }

    @Test
    void updateTask_WhenDeadlineNotChanged_ShouldNotRecalculatePriority() {
        Task existing = buildTask("t8", "student-1", TaskStatus.TODO, TaskPriority.LOW,
                LocalDateTime.now().plusDays(10));

        when(taskRepositoryPort.findById("t8")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // Request changes title only — deadline stays null (not being changed)
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated title")
                .build();

        Task result = updateTaskUseCase.updateTask("t8", "student-1", request);

        assertEquals(TaskPriority.LOW, result.getPriority(),
                "Priority should not change when deadline is not part of the update");
    }

    // ─── Task not found ──────────────────────────────────────────────────────────

    @Test
    void updateTask_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> updateTaskUseCase.updateTask("missing", "student-1", UpdateTaskRequest.builder().build()));

        verify(taskRepositoryPort, never()).save(any());
    }

    // ─── CA4: scheduledDate overlap → 409 ────────────────────────────────────────

    @Test
    void updateTask_WhenScheduledDateOverlapsAnotherTask_ShouldThrowTaskConflictException() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);
        Task existing = buildTask("t-update", "student-1", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDateTime.now().plusDays(10));

        // Another task occupies base → base+60min
        Task sibling = Task.builder()
                .id("t-sibling")
                .studentId("student-1")
                .status(TaskStatus.TODO)
                .scheduledDate(base)
                .estimatedDurationMinutes(60)
                .build();

        when(taskRepositoryPort.findById("t-update")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.findByStudentId("student-1")).thenReturn(List.of(existing, sibling));

        // Request scheduledDate that falls inside the sibling's window
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .scheduledDate(base.plusMinutes(30))
                .estimatedDurationMinutes(60)
                .build();

        TaskConflictException ex = assertThrows(TaskConflictException.class,
                () -> updateTaskUseCase.updateTask("t-update", "student-1", request));

        assertTrue(ex.getMessage().contains("Fecha sugerida"),
                "Exception message should include a suggested alternative date");
        verify(taskRepositoryPort, never()).save(any());
    }

    @Test
    void updateTask_WhenScheduledDateDoesNotOverlap_ShouldSaveSuccessfully() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);
        Task existing = buildTask("t-noop", "student-1", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDateTime.now().plusDays(10));

        // Sibling occupies base → base+60min; new scheduledDate is at base+120 (no overlap)
        Task sibling = Task.builder()
                .id("t-sibling2")
                .studentId("student-1")
                .status(TaskStatus.TODO)
                .scheduledDate(base)
                .estimatedDurationMinutes(60)
                .build();

        when(taskRepositoryPort.findById("t-noop")).thenReturn(Optional.of(existing));
        when(taskRepositoryPort.findByStudentId("student-1")).thenReturn(List.of(existing, sibling));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .scheduledDate(base.plusMinutes(120))
                .estimatedDurationMinutes(60)
                .build();

        Task result = updateTaskUseCase.updateTask("t-noop", "student-1", request);

        assertEquals(base.plusMinutes(120), result.getScheduledDate());
        verify(taskRepositoryPort).save(any(Task.class));
    }
}
