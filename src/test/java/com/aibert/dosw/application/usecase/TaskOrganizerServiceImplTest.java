package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
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
}
