package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.application.mapper.TaskDtoMapper;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.in.OrganizeTasksUseCase;
import com.aibert.dosw.domain.ports.in.TaskOrganizerUseCase;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock private CreateTaskUseCase createTaskUseCase;
    @Mock private GetTasksUseCase getTasksUseCase;
    @Mock private OrganizeTasksUseCase organizeTasksUseCase;
    @Mock private TaskOrganizerUseCase taskOrganizerUseCase;
    @Mock private UpdateTaskStatusUseCase updateTaskStatusUseCase;
    @Mock private GetTasksForViewUseCase getTasksForViewUseCase;
    @Mock private TaskDtoMapper taskDtoMapper;

    @InjectMocks
    private TaskController taskController;

    @Test
    void createTask_ShouldReturnCreatedResponse() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test title")
                .studentId("S123")
                .priority(TaskPriority.HIGH)
                .estimatedDurationMinutes(60)
                .deadline(LocalDateTime.now().plusDays(1))
                .build();

        Task mappedTask  = Task.builder().title("Test title").build();
        Task createdTask = Task.builder().id("uuid").title("Test title").status(TaskStatus.TODO).build();
        TaskResponse expectedResponse = TaskResponse.builder().id("uuid").title("Test title").status(TaskStatus.TODO).build();

        when(taskDtoMapper.toModel(request)).thenReturn(mappedTask);
        when(createTaskUseCase.createTask(mappedTask)).thenReturn(createdTask);
        when(taskDtoMapper.toResponse(createdTask)).thenReturn(expectedResponse);

        ResponseEntity<TaskResponse> response = taskController.createTask(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(taskDtoMapper).toModel(request);
        verify(createTaskUseCase).createTask(mappedTask);
        verify(taskDtoMapper).toResponse(createdTask);
    }

    @Test
    void updateTaskStatus_ShouldReturn200WithUpdatedTask() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        Task updatedTask = Task.builder().id("t1").status(TaskStatus.IN_PROGRESS).build();
        TaskResponse expectedResponse = TaskResponse.builder().id("t1").status(TaskStatus.IN_PROGRESS).build();

        when(updateTaskStatusUseCase.updateStatus("t1", TaskStatus.IN_PROGRESS)).thenReturn(updatedTask);
        when(taskDtoMapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        ResponseEntity<TaskResponse> response = taskController.updateTaskStatus("t1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(updateTaskStatusUseCase).updateStatus("t1", TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTaskStatus_WhenCompleted_ShouldReturnTaskWithCompletedAt() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.COMPLETED);
        LocalDateTime now = LocalDateTime.now();
        Task updatedTask = Task.builder().id("t2").status(TaskStatus.COMPLETED).completedAt(now).build();
        TaskResponse expectedResponse = TaskResponse.builder().id("t2").status(TaskStatus.COMPLETED).completedAt(now).build();

        when(updateTaskStatusUseCase.updateStatus("t2", TaskStatus.COMPLETED)).thenReturn(updatedTask);
        when(taskDtoMapper.toResponse(updatedTask)).thenReturn(expectedResponse);

        ResponseEntity<TaskResponse> response = taskController.updateTaskStatus("t2", request);

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

        ResponseEntity<?> response = taskController.getTasks("S1", null, "kanban", null, null, null);

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

        when(getTasksForViewUseCase.getCalendarView("S2", TaskStatus.TODO, start, end)).thenReturn(List.of(t));
        when(taskDtoMapper.toResponse(t)).thenReturn(tr);

        ResponseEntity<?> response = taskController.getTasks("S2", null, "calendar", TaskStatus.TODO, start, end);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TaskResponse> body = (List<TaskResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(getTasksForViewUseCase).getCalendarView("S2", TaskStatus.TODO, start, end);
    }
}
