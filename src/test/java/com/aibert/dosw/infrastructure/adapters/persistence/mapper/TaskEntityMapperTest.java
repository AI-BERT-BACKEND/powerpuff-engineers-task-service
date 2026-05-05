package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskEntityMapperTest {

    private TaskEntityMapper mapper;
    private final LocalDateTime deadline = LocalDateTime.now().plusDays(3);

    @BeforeEach
    void setUp() {
        mapper = new TaskEntityMapper();
    }

    @Test
    void toEntity_ShouldMapAllFieldsFromTask() {
        Task task = Task.builder()
                .id("task-1")
                .studentId("S1")
                .subjectId("MATH-101")
                .title("Test Task")
                .description("Description")
                .estimatedDurationMinutes(60)
                .deadline(deadline)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .scheduledDate(deadline.plusHours(1))
                .completedAt(null)
                .build();

        TaskEntity result = mapper.toEntity(task);

        assertEquals("task-1", result.getId());
        assertEquals("S1", result.getStudentId());
        assertEquals("MATH-101", result.getSubjectId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(60, result.getEstimatedDurationMinutes());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertNotNull(result.getScheduledDate());
        assertNull(result.getCompletedAt());
    }

    @Test
    void toDomain_ShouldMapAllFieldsFromEntity() {
        TaskEntity entity = TaskEntity.builder()
                .id("task-1")
                .studentId("S1")
                .subjectId("MATH-101")
                .title("Test Task")
                .description("Description")
                .estimatedDurationMinutes(60)
                .deadline(deadline)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.IN_PROGRESS)
                .scheduledDate(deadline.plusHours(2))
                .completedAt(null)
                .build();

        Task result = mapper.toDomain(entity);

        assertEquals("task-1", result.getId());
        assertEquals("S1", result.getStudentId());
        assertEquals("MATH-101", result.getSubjectId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(60, result.getEstimatedDurationMinutes());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getScheduledDate());
        assertNull(result.getCompletedAt());
    }
}
