package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskOrganizerServiceImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private TaskOrganizerServiceImpl taskOrganizerService;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        task1 = Task.builder()
                .id("1")
                .title("Task 1")
                .priority(TaskPriority.LOW)
                .deadline(LocalDateTime.now().plusDays(5))
                .subjectId("MATH")
                .build();

        task2 = Task.builder()
                .id("2")
                .title("Task 2")
                .priority(TaskPriority.MEDIUM)
                .deadline(LocalDateTime.now().plusHours(12))
                .subjectId("PHYSICS")
                .build();

        task3 = Task.builder()
                .id("3")
                .title("Task 3")
                .priority(TaskPriority.CRITICAL)
                .deadline(LocalDateTime.now().plusHours(30))
                .subjectId("CHEMISTRY")
                .build();
    }

    @Test
    void getOrganizedTasks_shouldRecalculatePriorityAndSortByDefaultPriority() {
        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(Arrays.asList(task1, task2, task3));

        List<Task> result = taskOrganizerService.getOrganizedTasks("student1", null);

        assertEquals(TaskPriority.HIGH, task2.getPriority());
        verify(taskRepositoryPort, times(1)).saveAll(anyList());

        assertEquals("3", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
        assertEquals("1", result.get(2).getId());
    }

    @Test
    void getOrganizedTasks_shouldSortByDeadline() {
        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(Arrays.asList(task1, task2, task3));

        List<Task> result = taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.DEADLINE);

        assertEquals("2", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
        assertEquals("1", result.get(2).getId());
    }

    @Test
    void getOrganizedTasks_shouldSortBySubject() {
        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(Arrays.asList(task1, task2, task3));

        List<Task> result = taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.SUBJECT);

        assertEquals("3", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("2", result.get(2).getId());
    }

    @Test
    void getOrganizedTasks_WhenDeadlineIsNull_ShouldSkipEscalationAndNotSave() {
        Task taskNullDeadline = Task.builder()
                .id("1").title("Task no deadline")
                .priority(TaskPriority.LOW)
                .deadline(null)
                .subjectId("MATH")
                .build();

        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(List.of(taskNullDeadline));

        taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.PRIORITY);

        verify(taskRepositoryPort, never()).saveAll(anyList());
        assertEquals(TaskPriority.LOW, taskNullDeadline.getPriority());
    }

    @Test
    void getOrganizedTasks_WhenPastDeadline_ShouldNotEscalate() {
        Task pastTask = Task.builder()
                .id("1").title("Past deadline task")
                .priority(TaskPriority.MEDIUM)
                .deadline(LocalDateTime.now().minusHours(5))
                .subjectId("MATH")
                .build();

        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(List.of(pastTask));

        taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.PRIORITY);

        verify(taskRepositoryPort, never()).saveAll(anyList());
        assertEquals(TaskPriority.MEDIUM, pastTask.getPriority());
    }

    @Test
    void getOrganizedTasks_WhenUrgentButAlreadyHighPriority_ShouldNotEscalate() {
        Task highTask = Task.builder()
                .id("1").title("High priority urgent")
                .priority(TaskPriority.HIGH)
                .deadline(LocalDateTime.now().plusHours(12))
                .subjectId("MATH")
                .build();

        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(List.of(highTask));

        taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.PRIORITY);

        verify(taskRepositoryPort, never()).saveAll(anyList());
        assertEquals(TaskPriority.HIGH, highTask.getPriority());
    }

    @Test
    void getOrganizedTasks_WhenUrgentButCriticalPriority_ShouldNotEscalate() {
        Task criticalTask = Task.builder()
                .id("1").title("Critical urgent task")
                .priority(TaskPriority.CRITICAL)
                .deadline(LocalDateTime.now().plusHours(12))
                .subjectId("MATH")
                .build();

        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(List.of(criticalTask));

        taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.PRIORITY);

        verify(taskRepositoryPort, never()).saveAll(anyList());
        assertEquals(TaskPriority.CRITICAL, criticalTask.getPriority());
    }

    @Test
    void getOrganizedTasks_WhenNullPriority_ShouldTreatAsLowestScore() {
        Task nullPriorityTask = Task.builder()
                .id("1").title("Null priority task")
                .priority(null)
                .deadline(LocalDateTime.now().plusDays(5))
                .subjectId("MATH")
                .build();

        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(List.of(nullPriorityTask));

        List<Task> result = taskOrganizerService.getOrganizedTasks("student1", SortCriteriaEnum.PRIORITY);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepositoryPort, never()).saveAll(anyList());
    }

    @Test
    void getPrioritizedActiveTasks_shouldReturnOnlyTodoAndInProgressTasks() {
        Task todoTask = Task.builder()
                .id("1").title("Todo task")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .deadline(LocalDateTime.now().plusDays(3))
                .subjectId("MATH")
                .build();

        Task inProgressTask = Task.builder()
                .id("2").title("In progress task")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .deadline(LocalDateTime.now().plusDays(1))
                .subjectId("PHYSICS")
                .build();

        Task completedTask = Task.builder()
                .id("3").title("Completed task")
                .priority(TaskPriority.CRITICAL)
                .status(TaskStatus.COMPLETED)
                .deadline(LocalDateTime.now().plusDays(5))
                .subjectId("CHEMISTRY")
                .build();

        Task pausedTask = Task.builder()
                .id("4").title("Paused task")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.PAUSED)
                .deadline(LocalDateTime.now().plusDays(2))
                .subjectId("BIOLOGY")
                .build();

        when(taskRepositoryPort.findByStudentId("student1"))
                .thenReturn(Arrays.asList(todoTask, inProgressTask, completedTask, pausedTask));

        List<Task> result = taskOrganizerService.getPrioritizedActiveTasks("student1");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getStatus() == TaskStatus.TODO || t.getStatus() == TaskStatus.IN_PROGRESS));
        assertEquals("2", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
    }

    @Test
    void getPrioritizedActiveTasks_shouldReturnEmptyWhenNoActiveTasks() {
        Task completedTask = Task.builder()
                .id("1").title("Completed")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.COMPLETED)
                .deadline(LocalDateTime.now().plusDays(1))
                .subjectId("MATH")
                .build();

        when(taskRepositoryPort.findByStudentId("student1")).thenReturn(List.of(completedTask));

        List<Task> result = taskOrganizerService.getPrioritizedActiveTasks("student1");

        assertEquals(0, result.size());
    }
}
