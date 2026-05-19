package com.aibert.dosw.application.usecase;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestoreTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private RestoreTaskUseCaseImpl restoreTaskUseCase;

    private Task buildDeletedTask(String id, String studentId) {
        return Task.builder()
                .id(id)
                .studentId(studentId)
                .title("Deleted Task")
                .subjectId("sub-1")
                .taskType(TaskType.TAREA)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(5))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .deletedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    // ─── Happy path ──────────────────────────────────────────────────────────────

    @Test
    void restoreTask_WhenOwnerAndTaskWasDeleted_ShouldReturnRestoredTask() {
        Task task = buildDeletedTask("r1", "student-1");
        when(taskRepositoryPort.restore("r1")).thenReturn(Optional.of(task));

        Task result = restoreTaskUseCase.restoreTask("r1", "student-1");

        assertNotNull(result);
        assertEquals("r1", result.getId());
        verify(taskRepositoryPort).restore("r1");
        verify(taskRepositoryPort, never()).softDelete(any());
    }

    // ─── Not found ───────────────────────────────────────────────────────────────

    @Test
    void restoreTask_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.restore("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> restoreTaskUseCase.restoreTask("missing", "student-1"));
    }

    // ─── Ownership ───────────────────────────────────────────────────────────────

    @Test
    void restoreTask_WhenStudentIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task task = buildDeletedTask("r2", "owner-student");
        when(taskRepositoryPort.restore("r2")).thenReturn(Optional.of(task));

        assertThrows(TaskForbiddenException.class,
                () -> restoreTaskUseCase.restoreTask("r2", "other-student"));

        // Task must be re-soft-deleted since the wrong student triggered the restore
        verify(taskRepositoryPort).softDelete("r2");
    }
}
