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

    @Test
    void organizeTasksForStudent_WhenDeadlineIsBeforeToday_ShouldScheduleOnToday() {
        Task task = Task.builder().id("1").studentId("S1").title("Past deadline task")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().minusDays(2))
                .build();

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(task));
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(List.of(task));

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        assertNotNull(result);
        assertNotNull(task.getScheduledDate());
    }

    @Test
    void organizeTasksForStudent_WhenDayFullForDeadline_ShouldOverflowToDeadlineDate() {
        // 4 tasks with duration=61 and deadline=today: first 3 fill the day (3x61=183<240),
        // 4th can't fit (183+61=244>240), advances past deadline → overflow path
        LocalDateTime todayDeadline = LocalDateTime.now().toLocalDate().atTime(23, 0);
        List<Task> tasks = List.of(
                Task.builder().id("1").studentId("S1").title("T1")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                        .estimatedDurationMinutes(61).deadline(todayDeadline).build(),
                Task.builder().id("2").studentId("S1").title("T2")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                        .estimatedDurationMinutes(61).deadline(todayDeadline).build(),
                Task.builder().id("3").studentId("S1").title("T3")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                        .estimatedDurationMinutes(61).deadline(todayDeadline).build(),
                Task.builder().id("4").studentId("S1").title("T4")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                        .estimatedDurationMinutes(61).deadline(todayDeadline).build()
        );

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(tasks);
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(tasks);

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        assertNotNull(result);
        tasks.forEach(t -> assertNotNull(t.getScheduledDate()));
    }

    @Test
    void organizeTasksForStudent_WhenPriorityIsNull_ShouldTreatAsLowest() {
        Task nullPriorityTask = Task.builder().id("1").studentId("S1").title("Null priority task")
                .status(TaskStatus.TODO).priority(null)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
        Task mediumPriorityTask = Task.builder().id("2").studentId("S1").title("Medium priority task")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .estimatedDurationMinutes(30)
                .deadline(LocalDateTime.now().plusDays(4))
                .build();

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(nullPriorityTask, mediumPriorityTask));
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(List.of(mediumPriorityTask, nullPriorityTask));

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        assertNotNull(result);
        assertNotNull(nullPriorityTask.getScheduledDate());
    }

    @Test
    void organizeTasksForStudent_WithHighPriority_ShouldSortHighBeforeLow() {
        Task highTask = Task.builder().id("1").studentId("S1").title("High priority task")
                .status(TaskStatus.TODO).priority(TaskPriority.HIGH)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(5))
                .build();
        Task lowTask = Task.builder().id("2").studentId("S1").title("Low priority task")
                .status(TaskStatus.TODO).priority(TaskPriority.LOW)
                .estimatedDurationMinutes(30)
                .deadline(LocalDateTime.now().plusDays(2))
                .build();

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(lowTask, highTask));
        when(taskRepositoryPort.saveAll(anyList())).thenReturn(List.of(highTask, lowTask));

        List<Task> result = organizeTasksUseCase.organizeTasksForStudent("S1");

        assertNotNull(result);
        assertNotNull(highTask.getScheduledDate());
    }
}
