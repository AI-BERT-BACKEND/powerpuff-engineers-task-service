package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateDeadlineRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.DailySummaryResponse;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.dto.response.UpdateTaskStatusResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.DeleteTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetDailySummaryUseCase;
import com.aibert.dosw.domain.ports.in.GetTaskByIdUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.in.GetCalendarConflictsUseCase;
import com.aibert.dosw.domain.ports.in.RescheduleTaskUseCase;
import com.aibert.dosw.domain.ports.in.UpdateDeadlineUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock private CreateTaskUseCase createTaskUseCase;
    @Mock private GetTasksUseCase getTasksUseCase;
    @Mock private OrganizeTasksUseCase organizeTasksUseCase;
    @Mock private TaskOrganizerUseCase taskOrganizerUseCase;
    @Mock private UpdateTaskStatusUseCase updateTaskStatusUseCase;
    @Mock private UpdateTaskUseCase updateTaskUseCase;
    @Mock private DeleteTaskUseCase deleteTaskUseCase;
    @Mock private GetTasksForViewUseCase getTasksForViewUseCase;
    @Mock private GetTaskByIdUseCase getTaskByIdUseCase;
    @Mock private GetDailySummaryUseCase getDailySummaryUseCase;
    @Mock private RescheduleTaskUseCase rescheduleTaskUseCase;
    @Mock private GetCalendarConflictsUseCase getCalendarConflictsUseCase;
    @Mock private UpdateDeadlineUseCase updateDeadlineUseCase;
    @Mock private TaskDtoMapper taskDtoMapper;

    @InjectMocks
    private TaskController taskController;

    @Test
    void createTask_ShouldReturnCreatedResponse() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test title")
                .taskType(TaskType.TAREA)
                .priority(TaskPriority.HIGH)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(1))
                .build();

        Task mappedTask  = Task.builder().title("Test title").build();
        Task createdTask = Task.builder().id("uuid").title("Test title").status(TaskStatus.TODO).build();
        TaskResponse expectedResponse = TaskResponse.builder().id("uuid").title("Test title").status(TaskStatus.TODO).build();

        when(taskDtoMapper.toModel(request, "S123")).thenReturn(mappedTask);
        when(createTaskUseCase.createTask(mappedTask)).thenReturn(createdTask);
        when(taskDtoMapper.toResponse(createdTask)).thenReturn(expectedResponse);

        ResponseEntity<TaskResponse> response = taskController.createTask("S123", request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(taskDtoMapper).toModel(request, "S123");
        verify(taskDtoMapper).toResponse(createdTask);
    }

    // AIB-18.4 — PATCH /{id}/status returns UpdateTaskStatusResponse with message
    @Test
    void updateTaskStatus_ShouldReturn200WithStatusResponse() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        LocalDateTime changedAt = LocalDateTime.now();
        Task updatedTask = Task.builder()
                .id("t1")
                .studentId("S1")
                .status(TaskStatus.IN_PROGRESS)
                .statusChangedAt(changedAt)
                .build();

        when(updateTaskStatusUseCase.updateStatus("t1", "S1", TaskStatus.IN_PROGRESS)).thenReturn(updatedTask);

        ResponseEntity<UpdateTaskStatusResponse> response = taskController.updateTaskStatus("t1", "S1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("t1", response.getBody().getTaskId());
        assertEquals("S1", response.getBody().getStudentId());
        assertEquals(TaskStatus.IN_PROGRESS, response.getBody().getStatus());
        assertEquals(changedAt, response.getBody().getChangedAt());
        assertEquals("Tarea actualizada exitosamente", response.getBody().getMessage());
        verify(updateTaskStatusUseCase).updateStatus("t1", "S1", TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTaskStatus_WhenCompleted_ShouldIncludeCompletedAt() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.COMPLETED);
        LocalDateTime now = LocalDateTime.now();
        Task updatedTask = Task.builder()
                .id("t2")
                .studentId("S1")
                .status(TaskStatus.COMPLETED)
                .completedAt(now)
                .statusChangedAt(now)
                .build();

        when(updateTaskStatusUseCase.updateStatus("t2", "S1", TaskStatus.COMPLETED)).thenReturn(updatedTask);

        ResponseEntity<UpdateTaskStatusResponse> response = taskController.updateTaskStatus("t2", "S1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getCompletedAt());
        assertNotNull(response.getBody().getChangedAt());
    }

    @Test
    void getTasks_WithKanbanView_ShouldReturnKanbanGroupedResponse() {
        Task t1 = Task.builder().id("1").status(TaskStatus.TODO).build();
        TaskResponse tr1 = TaskResponse.builder().id("1").status(TaskStatus.TODO).build();

        Map<TaskStatus, List<Task>> kanbanMap = Map.of(TaskStatus.TODO, List.of(t1));

        when(getTasksForViewUseCase.getKanbanView("S1")).thenReturn(kanbanMap);
        when(taskDtoMapper.toResponse(t1)).thenReturn(tr1);

        ResponseEntity<?> response = taskController.getTasks("S1", null, "kanban", null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(KanbanResponse.class, response.getBody());
        KanbanResponse body = (KanbanResponse) response.getBody();
        assertEquals(1, body.getTodo().size());
        assertEquals(0, body.getInProgress().size());
        verify(getTasksForViewUseCase).getKanbanView("S1");
    }

    // AIB-20 FA-02 — kanban view with status filter returns only the matching column
    @Test
    void getTasks_WithKanbanViewAndStatusFilter_ShouldReturnOnlyMatchingColumn() {
        Task t1 = Task.builder().id("1").status(TaskStatus.TODO).build();
        TaskResponse tr1 = TaskResponse.builder().id("1").status(TaskStatus.TODO).build();
        Task t2 = Task.builder().id("2").status(TaskStatus.COMPLETED).build();

        Map<TaskStatus, List<Task>> kanbanMap = Map.of(
                TaskStatus.TODO, List.of(t1),
                TaskStatus.COMPLETED, List.of(t2));

        when(getTasksForViewUseCase.getKanbanView("S1")).thenReturn(kanbanMap);
        when(taskDtoMapper.toResponse(t1)).thenReturn(tr1);

        ResponseEntity<?> response = taskController.getTasks("S1", null, "kanban", TaskStatus.TODO, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(KanbanResponse.class, response.getBody());
        KanbanResponse body = (KanbanResponse) response.getBody();
        assertEquals(1, body.getTodo().size());
        assertEquals(0, body.getInProgress().size());
        assertEquals(0, body.getCompleted().size());
        verify(taskDtoMapper, never()).toResponse(t2);
    }

    @Test
    void getTasks_WithCalendarView_ShouldReturnFilteredTaskList() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(7);
        Task t = Task.builder().id("5").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("5").status(TaskStatus.TODO).build();

        when(getTasksForViewUseCase.getCalendarView("S2", TaskStatus.TODO, start, end, null, null)).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S2", null, "calendar", TaskStatus.TODO, start, end, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(getTasksForViewUseCase).getCalendarView("S2", TaskStatus.TODO, start, end, null, null);
    }

    @Test
    void getTasks_WithDefaultView_ShouldReturnSortedList() {
        Task t = Task.builder().id("3").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("3").status(TaskStatus.TODO).build();

        when(taskOrganizerUseCase.getOrganizedTasks("S3", null, null)).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S3", null, null, null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(taskOrganizerUseCase).getOrganizedTasks("S3", null, null);
    }

    @Test
    void getTasks_WithLimitParam_ShouldReturnLimitedList() {
        Task t = Task.builder().id("3").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("3").status(TaskStatus.TODO).build();

        when(taskOrganizerUseCase.getOrganizedTasks("S3", null, 5)).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S3", null, null, null, null, null, null, null, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(taskOrganizerUseCase).getOrganizedTasks("S3", null, 5);
    }

    @Test
    void getTasksByStudentId_ShouldReturnOkWithList() {
        Task t = Task.builder().id("10").studentId("S4").status(TaskStatus.IN_PROGRESS).build();
        TaskResponse tr = TaskResponse.builder().id("10").status(TaskStatus.IN_PROGRESS).build();

        when(getTasksUseCase.getTasksByStudentId("S4")).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasksByStudentId("S4", "S4");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(getTasksUseCase).getTasksByStudentId("S4");
    }

    @Test
    void getTasksByStudentId_WhenUserIdMismatch_ShouldReturn403() {
        ResponseEntity<?> response = taskController.getTasksByStudentId("OTHER", "S4");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(getTasksUseCase, never()).getTasksByStudentId(any());
    }

    @Test
    void organizeTasks_ShouldReturnOkWithOrganizedTasks() {
        Task t = Task.builder().id("20").studentId("S5").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("20").status(TaskStatus.TODO).build();

        when(organizeTasksUseCase.organizeTasksForStudent("S5")).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.organizeTasks("S5", "S5");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(organizeTasksUseCase).organizeTasksForStudent("S5");
    }

    @Test
    void organizeTasks_WhenUserIdMismatch_ShouldReturn403() {
        ResponseEntity<?> response = taskController.organizeTasks("OTHER", "S5");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(organizeTasksUseCase, never()).organizeTasksForStudent(any());
    }

    @Test
    void getTaskById_WhenExists_ShouldReturn200WithTaskResponse() {
        Task task = Task.builder().id("task-abc").studentId("S6").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("task-abc").status(TaskStatus.TODO).build();

        when(getTaskByIdUseCase.getTaskById("task-abc")).thenReturn(task);
        when(taskDtoMapper.toResponse(task)).thenReturn(tr);

        ResponseEntity<TaskResponse> response = taskController.getTaskById("task-abc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("task-abc", response.getBody().getId());
        verify(getTaskByIdUseCase).getTaskById("task-abc");
    }

    @Test
    void getTasks_WithCalendarViewAndSubjectFilter_ShouldPassSubjectIdToUseCase() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(7);
        Task t = Task.builder().id("7").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("7").status(TaskStatus.TODO).build();

        when(getTasksForViewUseCase.getCalendarView("S7", null, start, end, "sub-1", null))
                .thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S7", null, "calendar", null, start, end, "sub-1", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(getTasksForViewUseCase).getCalendarView("S7", null, start, end, "sub-1", null);
    }

    @Test
    void updateTask_ShouldReturn200WithUpdatedTask() {
        UpdateTaskRequest request = UpdateTaskRequest.builder().title("Updated title").build();
        Task updated = Task.builder().id("u1").studentId("S8").title("Updated title").build();
        TaskResponse tr = TaskResponse.builder().id("u1").title("Updated title").build();

        when(updateTaskUseCase.updateTask("u1", "S8", request)).thenReturn(updated);
        when(taskDtoMapper.toResponse(updated)).thenReturn(tr);

        ResponseEntity<TaskResponse> response = taskController.updateTask("u1", "S8", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated title", response.getBody().getTitle());
        verify(updateTaskUseCase).updateTask("u1", "S8", request);
    }

    // AIB-18.3 — DELETE /{id}: permanent deletion, HTTP 200 with message
    @Test
    void deleteTask_WhenOwner_ShouldReturn200WithMessage() {
        doNothing().when(deleteTaskUseCase).deleteTask("d1", "S9");

        ResponseEntity<Map<String, String>> response = taskController.deleteTask("d1", "S9");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tarea eliminada exitosamente", response.getBody().get("message"));
        verify(deleteTaskUseCase).deleteTask("d1", "S9");
    }

    // AIB-21 RN-02 — PATCH /{id}/deadline updates the deadline and returns the updated task
    @Test
    void updateDeadline_ShouldReturn200WithUpdatedTask() {
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(3);
        UpdateDeadlineRequest request = UpdateDeadlineRequest.builder().newDeadline(newDeadline).build();
        Task updated = Task.builder().id("d1").studentId("S10").deadline(newDeadline).build();
        TaskResponse tr = TaskResponse.builder().id("d1").deadline(newDeadline).build();

        when(updateDeadlineUseCase.updateDeadline("d1", "S10", newDeadline)).thenReturn(updated);
        when(taskDtoMapper.toResponse(updated)).thenReturn(tr);

        ResponseEntity<TaskResponse> response = taskController.updateDeadline("d1", "S10", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newDeadline, response.getBody().getDeadline());
        verify(updateDeadlineUseCase).updateDeadline("d1", "S10", newDeadline);
    }

    @Test
    void getDailySummary_ShouldReturn200WithSummary() {
        DailySummaryResponse summary = DailySummaryResponse.builder()
                .completionPercentage(75)
                .totalScheduledHours(4.5)
                .completedCount(3)
                .pendingCount(1)
                .build();

        when(getDailySummaryUseCase.getDailySummary("S1")).thenReturn(summary);

        ResponseEntity<DailySummaryResponse> response = taskController.getDailySummary("S1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(75, response.getBody().getCompletionPercentage());
        assertEquals(3, response.getBody().getCompletedCount());
        verify(getDailySummaryUseCase).getDailySummary("S1");
    }
}
