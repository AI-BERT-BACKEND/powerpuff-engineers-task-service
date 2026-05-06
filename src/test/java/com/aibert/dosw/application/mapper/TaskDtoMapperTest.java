package com.aibert.dosw.application.mapper;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskDtoMapperTest {

    private TaskDtoMapper mapper;
    private final LocalDateTime deadline = LocalDateTime.now().plusDays(3);

    @BeforeEach
    void setUp() {
        mapper = new TaskDtoMapper();
    }

    @Test
    void toModel_ShouldMapAllFieldsFromRequest() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .description("Description")
                .estimatedDurationMinutes(90)
                .deadline(deadline)
                .priority(TaskPriority.HIGH)
                .subjectId("MATH-101")
                .build();

        Task result = mapper.toModel(request, "S1");

        assertEquals("Test Task", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(90, result.getEstimatedDurationMinutes());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals("S1", result.getStudentId());
        assertEquals("MATH-101", result.getSubjectId());
    }

    @Test
    void toResponse_ShouldMapAllFieldsFromTask() {
        Task task = Task.builder()
                .id("task-1")
                .studentId("S1")
                .subjectId("MATH-101")
                .title("Test Task")
                .description("Description")
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
        assertEquals(90, result.getEstimatedDurationMinutes());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertNotNull(result.getScheduledDate());
        assertNull(result.getCompletedAt());
    }
}
