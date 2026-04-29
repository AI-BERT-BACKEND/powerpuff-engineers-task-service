package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTasksForViewUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @Mock
    private TaskDtoMapper taskDtoMapper;

    @InjectMocks
    private GetTasksForViewUseCaseImpl getTasksForViewUseCase;

    // ─── AC1: Kanban view ─────────────────────────────────────────────────────

    @Test
    void getKanbanView_ShouldGroupTasksByStatus() {
        // Arrange
        Task todo       = task("1", TaskStatus.TODO);
        Task inProgress = task("2", TaskStatus.IN_PROGRESS);
        Task completed  = task("3", TaskStatus.COMPLETED);

        when(taskRepositoryPort.findByStudentId("S1")).thenReturn(List.of(todo, inProgress, completed));
        when(taskDtoMapper.toResponse(todo)).thenReturn(response("1", TaskStatus.TODO));
        when(taskDtoMapper.toResponse(inProgress)).thenReturn(response("2", TaskStatus.IN_PROGRESS));
        when(taskDtoMapper.toResponse(completed)).thenReturn(response("3", TaskStatus.COMPLETED));

        // Act
        KanbanResponse kanban = getTasksForViewUseCase.getKanbanView("S1");

        // Assert
        assertNotNull(kanban);
        assertEquals(1, kanban.getTodo().size());
        assertEquals(1, kanban.getInProgress().size());
        assertEquals(1, kanban.getCompleted().size());
        assertEquals("1", kanban.getTodo().get(0).getId());
        assertEquals("2", kanban.getInProgress().get(0).getId());
        assertEquals("3", kanban.getCompleted().get(0).getId());
    }

    @Test
    void getKanbanView_WhenNoTasks_ShouldReturnEmptyGroups() {
        // Arrange
        when(taskRepositoryPort.findByStudentId("S_EMPTY")).thenReturn(List.of());

        // Act
        KanbanResponse kanban = getTasksForViewUseCase.getKanbanView("S_EMPTY");

        // Assert
        assertNotNull(kanban);
        assertTrue(kanban.getTodo().isEmpty());
        assertTrue(kanban.getInProgress().isEmpty());
        assertTrue(kanban.getCompleted().isEmpty());
    }

    @Test
    void getKanbanView_WithMultipleTasksPerStatus_ShouldGroupCorrectly() {
        // Arrange
        Task todo1 = task("1", TaskStatus.TODO);
        Task todo2 = task("2", TaskStatus.TODO);
        Task done  = task("3", TaskStatus.COMPLETED);

        when(taskRepositoryPort.findByStudentId("S2")).thenReturn(List.of(todo1, todo2, done));
        when(taskDtoMapper.toResponse(todo1)).thenReturn(response("1", TaskStatus.TODO));
        when(taskDtoMapper.toResponse(todo2)).thenReturn(response("2", TaskStatus.TODO));
        when(taskDtoMapper.toResponse(done)).thenReturn(response("3", TaskStatus.COMPLETED));

        // Act
        KanbanResponse kanban = getTasksForViewUseCase.getKanbanView("S2");

        // Assert
        assertEquals(2, kanban.getTodo().size());
        assertTrue(kanban.getInProgress().isEmpty());
        assertEquals(1, kanban.getCompleted().size());
    }

    // ─── AC4, AC5, AC6: Calendar view ─────────────────────────────────────────

    @Test
    void getCalendarView_ShouldDelegateFiltersToRepository() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(7);
        Task t = task("5", TaskStatus.TODO);

        when(taskRepositoryPort.findByStudentIdWithFilters("S3", TaskStatus.TODO, start, end))
                .thenReturn(List.of(t));

        // Act
        List<Task> result = getTasksForViewUseCase.getCalendarView("S3", TaskStatus.TODO, start, end);

        // Assert
        assertEquals(1, result.size());
        verify(taskRepositoryPort).findByStudentIdWithFilters("S3", TaskStatus.TODO, start, end);
    }

    @Test
    void getCalendarView_WithNullFilters_ShouldReturnAllTasks() {
        // Arrange
        when(taskRepositoryPort.findByStudentIdWithFilters("S4", null, null, null))
                .thenReturn(List.of(task("6", TaskStatus.TODO), task("7", TaskStatus.COMPLETED)));

        // Act
        List<Task> result = getTasksForViewUseCase.getCalendarView("S4", null, null, null);

        // Assert
        assertEquals(2, result.size());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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

    private TaskResponse response(String id, TaskStatus status) {
        return TaskResponse.builder().id(id).status(status).build();
    }
}
