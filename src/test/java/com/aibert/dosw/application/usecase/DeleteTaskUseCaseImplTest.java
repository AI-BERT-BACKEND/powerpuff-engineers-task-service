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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTaskUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private DeleteTaskUseCaseImpl deleteTaskUseCase;

    private Task buildTask(String id, String studentId) {
        return Task.builder()
                .id(id)
                .studentId(studentId)
                .title("Task to delete")
                .subjectId("sub-1")
                .taskType(TaskType.TAREA)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(5))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .build();
    }

    // ─── Happy path ─────────────────────────────────────────────────────────────

    @Test
    void deleteTask_WhenOwnerAndTaskExists_ShouldDeleteSuccessfully() {
        Task task = buildTask("t1", "student-1");
        when(taskRepositoryPort.findById("t1")).thenReturn(Optional.of(task));

        deleteTaskUseCase.deleteTask("t1", "student-1");

        verify(taskRepositoryPort).deleteById("t1");
    }

    @Test
    void deleteTask_WhenOwnerAndTaskIsCompleted_ShouldDeleteSuccessfully() {
        Task task = buildTask("t2", "student-1");
        task.setStatus(TaskStatus.COMPLETED);
        when(taskRepositoryPort.findById("t2")).thenReturn(Optional.of(task));

        deleteTaskUseCase.deleteTask("t2", "student-1");

        verify(taskRepositoryPort).deleteById("t2");
    }

    // ─── Task not found ──────────────────────────────────────────────────────────

    @Test
    void deleteTask_WhenTaskNotFound_ShouldThrowTaskNotFoundException() {
        when(taskRepositoryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> deleteTaskUseCase.deleteTask("missing", "student-1"));

        verify(taskRepositoryPort, never()).deleteById(any());
    }

    // ─── RN-01: ownership ────────────────────────────────────────────────────────

    @Test
    void deleteTask_WhenStudentIsNotOwner_ShouldThrowTaskForbiddenException() {
        Task task = buildTask("t3", "owner-student");
        when(taskRepositoryPort.findById("t3")).thenReturn(Optional.of(task));

        assertThrows(TaskForbiddenException.class,
                () -> deleteTaskUseCase.deleteTask("t3", "other-student"));

        verify(taskRepositoryPort, never()).deleteById(any());
    }
}
