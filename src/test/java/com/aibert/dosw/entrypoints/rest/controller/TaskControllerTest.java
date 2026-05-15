package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.DeleteTaskUseCase;
import com.aibert.dosw.domain.ports.in.RestoreTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetTaskByIdUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
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
    @Mock private RestoreTaskUseCase restoreTaskUseCase;
    @Mock private GetTasksForViewUseCase getTasksForViewUseCase;
    @Mock private GetTaskByIdUseCase getTaskByIdUseCase;
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

    @Test
    void updateTaskStatus_ShouldReturn200WithUpdatedTask() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        Task updatedTask = Task.builder().id("t1").status(TaskStatus.IN_PROGRESS).build();
        TaskResponse expectedResponse = TaskResponse.builder().id("t1").status(TaskStatus.IN_PROGRESS).build();

        when(updateTaskStatusUseCase.updateStatus("t1", "S1", TaskStatus.IN_PROGRESS)).thenReturn(updatedTask);
        when(taskDtoMapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        ResponseEntity<TaskResponse> response = taskController.updateTaskStatus("t1", "S1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(updateTaskStatusUseCase).updateStatus("t1", "S1", TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTaskStatus_WhenCompleted_ShouldReturnTaskWithCompletedAt() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.COMPLETED);
        LocalDateTime now = LocalDateTime.now();
        Task updatedTask = Task.builder().id("t2").status(TaskStatus.COMPLETED).completedAt(now).build();
        TaskResponse expectedResponse = TaskResponse.builder().id("t2").status(TaskStatus.COMPLETED).completedAt(now).build();

        when(updateTaskStatusUseCase.updateStatus("t2", "S1", TaskStatus.COMPLETED)).thenReturn(updatedTask);
        when(taskDtoMapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        ResponseEntity<TaskResponse> response = taskController.updateTaskStatus("t2", "S1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getCompletedAt());
    }

    @Test
    void getTasks_WithKanbanView_ShouldReturnKanbanGroupedResponse() {
        Task t1 = Task.builder().id("1").status(TaskStatus.TODO).build();
        TaskResponse tr1 = TaskResponse.builder().id("1").status(TaskStatus.TODO).build();
        
        Map<TaskStatus, List<Task>> kanbanMap = Map.of(TaskStatus.TODO, List.of(t1));

        when(getTasksForViewUseCase.getKanbanView("S1")).thenReturn(kanbanMap);
        when(taskDtoMapper.toResponse(t1)).thenReturn(tr1);

        ResponseEntity<?> response = taskController.getTasks("S1", null, "kanban", null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(KanbanResponse.class, response.getBody());
        KanbanResponse body = (KanbanResponse) response.getBody();
        assertEquals(1, body.getTodo().size());
        assertEquals(0, body.getInProgress().size());
        verify(getTasksForViewUseCase).getKanbanView("S1");
    }

    @Test
    void getTasks_WithCalendarView_ShouldReturnFilteredTaskList() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = LocalDateTime.now().plusDays(7);
        Task t = Task.builder().id("5").status(TaskStatus.TODO).build();
        TaskResponse tr = TaskResponse.builder().id("5").status(TaskStatus.TODO).build();

        when(getTasksForViewUseCase.getCalendarView("S2", TaskStatus.TODO, start, end, null, null)).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S2", null, "calendar", TaskStatus.TODO, start, end, null, null);

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

        when(taskOrganizerUseCase.getOrganizedTasks("S3", null)).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S3", null, null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(taskOrganizerUseCase).getOrganizedTasks("S3", null);
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

    // R41 — GET /{id}
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

        ResponseEntity<?> response = taskController.getTasks("S7", null, "calendar", null, start, end, "sub-1", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(getTasksForViewUseCase).getCalendarView("S7", null, start, end, "sub-1", null);
    }

    // R39 — PATCH /{id}
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

    // R16 — DELETE /{id} (soft-delete, 204 No Content)
    @Test
    void deleteTask_WhenOwner_ShouldReturn204() {
        doNothing().when(deleteTaskUseCase).deleteTask("d1", "S9");

        ResponseEntity<Void> response = taskController.deleteTask("d1", "S9");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteTaskUseCase).deleteTask("d1", "S9");
    }

    // R16 — PATCH /{id}/restore (optional restore, 200)
    @Test
    void restoreTask_WhenOwnerAndTaskWasDeleted_ShouldReturn200() {
        Task task = Task.builder().id("r1").studentId("S10").status(TaskStatus.TODO).build();
        TaskResponse taskResponse = TaskResponse.builder().id("r1").status(TaskStatus.TODO).build();
        when(restoreTaskUseCase.restoreTask("r1", "S10")).thenReturn(task);
        when(taskDtoMapper.toResponse(task)).thenReturn(taskResponse);

        ResponseEntity<TaskResponse> result = taskController.restoreTask("r1", "S10");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(restoreTaskUseCase).restoreTask("r1", "S10");
    }
}
