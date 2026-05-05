package com.aibert.dosw.application.usecase;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizeTasksUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private OrganizeTasksUseCaseImpl organizeTasksUseCase;

    @Test
    void organizeTasksForStudent_ShouldSchedulePendingTasksAndReturnAll() {
        List<Task> tasks = List.of(
                Task.builder().id("1").studentId("S1").title("Task A")
                        .status(TaskStatus.TODO).priority(TaskPriority.HIGH)
                        .estimatedDurationMinutes(60)
                        .deadline(LocalDateTime.now().plusDays(3))
                        .build(),
                Task.builder().id("2").studentId("S1").title("Task B")
                        .status(TaskStatus.COMPLETED).priority(TaskPriority.LOW)
                        .estimatedDurationMinutes(30)
                        .deadline(LocalDateTime.now().plusDays(5))
                        .build()
        );
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(tasks);
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(List.of(tasks.get(0)));

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(tasks.get(0).getScheduledDate());
        verify(taskRepositoryPort).saveAll(anyList());
    }

    @Test
    void organizeTasksForStudent_WithMultiplePriorities_ShouldSortByCriticalFirst() {
        Task critical = Task.builder().id("1").studentId("S1").title("Critical Task")
                .status(TaskStatus.TODO).priority(TaskPriority.CRITICAL)
                .estimatedDurationMinutes(30).deadline(LocalDateTime.now().plusDays(5))
                .build();
        Task low = Task.builder().id("2").studentId("S1").title("Low Task")
                .status(TaskStatus.TODO).priority(TaskPriority.LOW)
                .estimatedDurationMinutes(30).deadline(LocalDateTime.now().plusDays(2))
                .build();

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(low, critical));
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(List.of(critical, low));

        organizeTasksUseCase.organizeTasksForStudent("S1");

        verify(taskRepositoryPort).saveAll(anyList());
    }

    @Test
    void organizeTasksForStudent_WhenNoTodoTasks_ShouldNotCallSaveAll() {
        List<Task> tasks = List.of(
                Task.builder().id("1").studentId("S1").title("Done Task")
                        .status(TaskStatus.COMPLETED).priority(TaskPriority.MEDIUM)
                        .estimatedDurationMinutes(60)
                        .deadline(LocalDateTime.now().plusDays(1))
                        .build()
        );
        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(tasks);

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        verify(taskRepositoryPort).saveAll(List.of());
        assertEquals(1, result.size());
    }

    @Test
    void organizeTasksForStudent_WhenEstimatedDurationIsNull_ShouldDefaultTo60Minutes() {
        Task task = Task.builder().id("1").studentId("S1").title("Task no duration")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .estimatedDurationMinutes(null)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(task));
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(List.of(task));

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        assertNotNull(result);
        assertNotNull(task.getScheduledDate());
    }
}
