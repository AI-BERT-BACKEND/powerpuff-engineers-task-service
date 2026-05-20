package com.aibert.dosw.application.dto;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.request.UpdateTaskStatusRequest;
import com.aibert.dosw.application.dto.response.KanbanResponse;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    private final LocalDateTime future = LocalDateTime.now().plusDays(5);

    // ── SubjectDTO ────────────────────────────────────────────────
    @Test
    void subjectDTO_Builder_ShouldSetAllFields() {
        SubjectDTO dto = SubjectDTO.builder()
                .id(1L).subjectName("Matemáticas").studentId("s-1")
                .teacherName("Prof. García").credits(4).semester("2026-1")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Matemáticas", dto.getSubjectName());
        assertEquals("s-1", dto.getStudentId());
        assertEquals("Prof. García", dto.getTeacherName());
        assertEquals(4, dto.getCredits());
    }

    @Test
    void subjectDTO_NoArgsConstructor_ShouldCreateEmpty() {
        SubjectDTO dto = new SubjectDTO();
        assertNull(dto.getId());
    }

    @Test
    void subjectDTO_Setters_ShouldWork() {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(2L);
        dto.setSubjectName("Física");
        assertEquals(2L, dto.getId());
        assertEquals("Física", dto.getSubjectName());
    }

    @Test
    void subjectDTO_Equals_ShouldReturnTrueForEqual() {
        SubjectDTO a = SubjectDTO.builder().id(1L).subjectName("A").build();
        SubjectDTO b = SubjectDTO.builder().id(1L).subjectName("A").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ── CreateTaskRequest ─────────────────────────────────────────
    @Test
    void createTaskRequest_Builder_ShouldSetAllFields() {
        CreateTaskRequest req = CreateTaskRequest.builder()
                .title("Tarea 1").description("Desc").estimatedDurationMinutes(60)
                .deadline(future).priority(TaskPriority.HIGH).subjectId("MATH-101")
                .build();

        assertEquals("Tarea 1", req.getTitle());
        assertEquals("Desc", req.getDescription());
        assertEquals(60, req.getEstimatedDurationMinutes());
        assertEquals(future, req.getDeadline());
        assertEquals(TaskPriority.HIGH, req.getPriority());
        assertEquals("MATH-101", req.getSubjectId());
    }

    @Test
    void createTaskRequest_NoArgsConstructor_ShouldCreateEmpty() {
        CreateTaskRequest req = new CreateTaskRequest();
        assertNull(req.getTitle());
    }

    @Test
    void createTaskRequest_Setters_ShouldWork() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Nueva tarea");
        req.setPriority(TaskPriority.LOW);
        assertEquals("Nueva tarea", req.getTitle());
        assertEquals(TaskPriority.LOW, req.getPriority());
    }

    // ── UpdateTaskStatusRequest ────────────────────────────────────
    @Test
    void updateTaskStatusRequest_Builder_ShouldSetStatus() {
        UpdateTaskStatusRequest req = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS).build();
        assertEquals(TaskStatus.IN_PROGRESS, req.getStatus());
    }

    @Test
    void updateTaskStatusRequest_Setter_ShouldWork() {
        UpdateTaskStatusRequest req = new UpdateTaskStatusRequest();
        req.setStatus(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, req.getStatus());
    }

    // ── TaskResponse ───────────────────────────────────────────────
    @Test
    void taskResponse_Builder_ShouldSetAllFields() {
        TaskResponse resp = TaskResponse.builder()
                .id("t-1").studentId("s-1").subjectId("MATH-101")
                .title("Tarea").description("Desc").estimatedDurationMinutes(45)
                .deadline(future).priority(TaskPriority.MEDIUM).status(TaskStatus.TODO)
                .scheduledDate(future).completedAt(null)
                .build();

        assertEquals("t-1", resp.getId());
        assertEquals("s-1", resp.getStudentId());
        assertEquals(TaskStatus.TODO, resp.getStatus());
        assertEquals(TaskPriority.MEDIUM, resp.getPriority());
        assertNull(resp.getCompletedAt());
    }

    @Test
    void taskResponse_Equals_ShouldWork() {
        TaskResponse a = TaskResponse.builder().id("1").title("A").build();
        TaskResponse b = TaskResponse.builder().id("1").title("A").build();
        assertEquals(a, b);
    }

    // ── KanbanResponse ─────────────────────────────────────────────
    @Test
    void kanbanResponse_Builder_ShouldSetAllLists() {
        TaskResponse task1 = TaskResponse.builder().id("1").status(TaskStatus.TODO).build();
        TaskResponse task2 = TaskResponse.builder().id("2").status(TaskStatus.IN_PROGRESS).build();
        TaskResponse task3 = TaskResponse.builder().id("3").status(TaskStatus.COMPLETED).build();

        KanbanResponse kanban = KanbanResponse.builder()
                .todo(List.of(task1))
                .inProgress(List.of(task2))
                .completed(List.of(task3))
                .build();

        assertEquals(1, kanban.getTodo().size());
        assertEquals(1, kanban.getInProgress().size());
        assertEquals(1, kanban.getCompleted().size());
        assertEquals("1", kanban.getTodo().get(0).getId());
    }

    @Test
    void kanbanResponse_NoArgsConstructor_ShouldCreateEmpty() {
        KanbanResponse kanban = new KanbanResponse();
        assertNull(kanban.getTodo());
    }

    @Test
    void kanbanResponse_Setters_ShouldWork() {
        KanbanResponse kanban = new KanbanResponse();
        kanban.setTodo(List.of());
        kanban.setInProgress(List.of());
        kanban.setCompleted(List.of());
        assertNotNull(kanban.getTodo());
        assertTrue(kanban.getInProgress().isEmpty());
    }
}
