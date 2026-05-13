package com.aibert.dosw.application.mapper;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskDtoMapperTest {

    private TaskDtoMapper mapper;
    private final LocalDateTime deadline = LocalDateTime.now().plusDays(3);

    @BeforeEach
    void setUp() {
        mapper = new TaskDtoMapperImpl();
    }

    @Test
    void toModel_ShouldMapAllFieldsFromRequest() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .description("Description")
                .taskType(TaskType.EXAMEN)
                .estimatedDurationMinutes(90)
                .deadline(deadline)
                .priority(TaskPriority.HIGH)
                .subjectId("MATH-101")
                .build();

        Task result = mapper.toModel(request, "S1");

        assertEquals("Test Task", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(TaskType.EXAMEN, result.getTaskType());
        assertEquals(90, result.getEstimatedDurationMinutes());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals("S1", result.getStudentId());
        assertEquals("MATH-101", result.getSubjectId());
    }

    @Test
    void toModel_ShouldMapNullPriorityAndDuration() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Minimal Task")
                .taskType(TaskType.TAREA)
                .deadline(deadline)
                .subjectId("BIO-101")
                .build();

        Task result = mapper.toModel(request, "S2");

        assertEquals("Minimal Task", result.getTitle());
        assertNull(result.getPriority());
        assertNull(result.getEstimatedDurationMinutes());
    }

    @Test
    void toResponse_ShouldMapAllFieldsFromTask() {
        Task task = Task.builder()
                .id("task-1")
                .studentId("S1")
                .subjectId("MATH-101")
                .title("Test Task")
                .description("Description")
                .taskType(TaskType.PROYECTO)
                .estimatedDurationMinutes(90)
                .deadline(deadline)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .scheduledDate(deadline.plusHours(1))
                .completedAt(null)
                .build();

        TaskResponse result = mapper.toResponse(task);

        assertEquals("task-1", result.getId());
        assertEquals("S1", result.getStudentId());
        assertEquals("MATH-101", result.getSubjectId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(TaskType.PROYECTO, result.getTaskType());
        assertEquals(90, result.getEstimatedDurationMinutes());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertNotNull(result.getScheduledDate());
    }

    @Test
    void toModel_WhenRequestIsNull_ShouldMapStudentIdOnly() {
        Task result = mapper.toModel(null, "S1");
        assertNotNull(result);
        assertEquals("S1", result.getStudentId());
    }

    @Test
    void toModel_WhenBothNull_ShouldReturnNull() {
        assertNull(mapper.toModel(null, null));
    }

    @Test
    void toResponse_WhenTaskIsNull_ShouldReturnNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toModel_ShouldNotMapIgnoredFields() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task")
                .taskType(TaskType.TAREA)
                .deadline(deadline)
                .subjectId("MATH-101")
                .build();

        Task result = mapper.toModel(request, "S1");

        assertNull(result.getId(),            "id must not be set by the mapper");
        assertNull(result.getStatus(),        "status must not be set by the mapper");
        assertNull(result.getScheduledDate(), "scheduledDate must not be set by the mapper");
        assertNull(result.getCompletedAt(),   "completedAt must not be set by the mapper");
    }

    @Test
    void toResponse_ShouldMapCompletedAtWhenPresent() {
        LocalDateTime completedAt = deadline.plusDays(1);
        Task task = Task.builder()
                .id("task-2")
                .studentId("S1")
                .subjectId("MATH-101")
                .title("Completed Task")
                .taskType(TaskType.TAREA)
                .deadline(deadline)
                .priority(TaskPriority.LOW)
                .status(TaskStatus.COMPLETED)
                .completedAt(completedAt)
                .build();

        TaskResponse result = mapper.toResponse(task);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertEquals(completedAt, result.getCompletedAt());
    }
}

