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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTasksForViewUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private GetTasksForViewUseCaseImpl getTasksForViewUseCase;

    @Test
    void getKanbanView_ShouldGroupTasksByStatus() {
        Task todo       = task("1", TaskStatus.TODO);
        Task inProgress = task("2", TaskStatus.IN_PROGRESS);
        Task completed  = task("3", TaskStatus.COMPLETED);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(todo, inProgress, completed));

        Map<TaskStatus, List<Task>> kanban = getTasksForViewUseCase.getKanbanView("S1");

        assertNotNull(kanban);
        assertEquals(1, kanban.get(TaskStatus.TODO).size());
        assertEquals(1, kanban.get(TaskStatus.IN_PROGRESS).size());
        assertEquals(1, kanban.get(TaskStatus.COMPLETED).size());
        assertEquals("1", kanban.get(TaskStatus.TODO).get(0).getId());
        assertEquals("2", kanban.get(TaskStatus.IN_PROGRESS).get(0).getId());
        assertEquals("3", kanban.get(TaskStatus.COMPLETED).get(0).getId());
    }

    @Test
    void getKanbanView_WhenNoTasks_ShouldReturnEmptyGroups() {
        when(taskRepositoryPort.findByStudentId("S_EMPTY")).thenReturn(List.of());

        Map<TaskStatus, List<Task>> kanban = getTasksForViewUseCase.getKanbanView("S_EMPTY");

        assertNotNull(kanban);
        assertTrue(kanban.isEmpty());
    }

    @Test
    void getKanbanView_WithMultipleTasksPerStatus_ShouldGroupCorrectly() {
        Task todo1 = task("1", TaskStatus.TODO);
        Task todo2 = task("2", TaskStatus.TODO);
        Task done  = task("3", TaskStatus.COMPLETED);

        when(taskRepositoryPort.findByStudentId("S2")).thenReturn(List.of(todo1, todo2, done));

        Map<TaskStatus, List<Task>> kanban = getTasksForViewUseCase.getKanbanView("S2");

        assertEquals(2, kanban.get(TaskStatus.TODO).size());
        assertNull(kanban.get(TaskStatus.IN_PROGRESS));
        assertEquals(1, kanban.get(TaskStatus.COMPLETED).size());
    }

    @Test
    void getCalendarView_ShouldDelegateFiltersToRepository() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(7);
        Task t = task("5", TaskStatus.TODO);

        when(taskRepositoryPort.findByStudentIdWithFilters("S3", TaskStatus.TODO, start, end, null, null))
                .thenReturn(List.of(t));

        List<Task> result = getTasksForViewUseCase.getCalendarView("S3", TaskStatus.TODO, start, end, null, null);

        assertEquals(1, result.size());
        verify(taskRepositoryPort).findByStudentIdWithFilters("S3", TaskStatus.TODO, start, end, null, null);
    }

    @Test
    void getCalendarView_WithNullFilters_ShouldReturnAllTasks() {
        when(taskRepositoryPort.findByStudentIdWithFilters("S4", null, null, null, null, null))
                .thenReturn(List.of(task("6", TaskStatus.TODO), task("7", TaskStatus.COMPLETED)));

        List<Task> result = getTasksForViewUseCase.getCalendarView("S4", null, null, null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void getKanbanView_WhenTaskIsPaused_ShouldExcludeItFromKanban() {
        // AIB-20 RN-03: Kanban board shows only TODO, IN_PROGRESS and COMPLETED
        Task pausedTask = Task.builder().id("5").studentId("S1").title("Paused task")
                .status(TaskStatus.PAUSED).priority(TaskPriority.MEDIUM)
                .deadline(LocalDateTime.now().plusDays(3)).build();
        Task todoTask = task("1", TaskStatus.TODO);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(pausedTask, todoTask));

        Map<TaskStatus, List<Task>> kanban = getTasksForViewUseCase.getKanbanView("S1");

        assertNull(kanban.get(TaskStatus.PAUSED));
        assertEquals(1, kanban.get(TaskStatus.TODO).size());
    }

    @Test
    void getKanbanView_WhenTaskHasNullStatus_ShouldExcludeItAndNotThrowNPE() {
        Task nullStatusTask = Task.builder().id("99").studentId("S1").title("Broken task").status(null).build();
        Task validTask = task("1", TaskStatus.TODO);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(nullStatusTask, validTask));

        Map<TaskStatus, List<Task>> kanban = getTasksForViewUseCase.getKanbanView("S1");

        assertNotNull(kanban);
        assertNull(kanban.get(null));
        assertEquals(1, kanban.get(TaskStatus.TODO).size());
    }

    private Task task(String id, TaskStatus status) {
        return Task.builder()
                .id(id)
                .studentId("S1")
                .title("Task " + id)
                .status(status)
                .priority(TaskPriority.MEDIUM)
                .deadline(LocalDateTime.now().plusDays(5))
                .estimatedDurationMinutes(60)
                .build();
    }
}
