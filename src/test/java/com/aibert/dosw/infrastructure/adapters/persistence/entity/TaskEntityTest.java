package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskEntityTest {

    private final LocalDateTime deadline = LocalDateTime.of(2026, 6, 1, 10, 0);
    private final LocalDateTime scheduled = LocalDateTime.of(2026, 5, 28, 9, 0);

    private TaskEntity buildEntity() {
        return TaskEntity.builder()
                .id("e1")
                .studentId("S1")
                .subjectId("MATH-101")
                .title("Task Title")
                .description("Some description")
                .taskType(TaskType.TAREA)
                .estimatedDurationMinutes(90)
                .deadline(deadline)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .scheduledDate(scheduled)
                .completedAt(null)
                .build();
    }

    @Test
    void builder_ShouldSetAllFields() {
        TaskEntity e = buildEntity();

        assertEquals("e1", e.getId());
        assertEquals("S1", e.getStudentId());
        assertEquals("MATH-101", e.getSubjectId());
        assertEquals("Task Title", e.getTitle());
        assertEquals("Some description", e.getDescription());
        assertEquals(TaskType.TAREA, e.getTaskType());
        assertEquals(90, e.getEstimatedDurationMinutes());
        assertEquals(deadline, e.getDeadline());
        assertEquals(TaskPriority.HIGH, e.getPriority());
        assertEquals(TaskStatus.TODO, e.getStatus());
        assertEquals(scheduled, e.getScheduledDate());
        assertNull(e.getCompletedAt());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyEntity() {
        TaskEntity e = new TaskEntity();
        assertNull(e.getId());
        assertNull(e.getStudentId());
        assertNull(e.getStatus());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        TaskEntity e = new TaskEntity("e1", "S1", "MATH-101", "Title", "Desc",
                TaskType.LECTURA, 60, deadline, TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, scheduled, null, null);

        assertEquals("e1", e.getId());
        assertEquals(TaskPriority.MEDIUM, e.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, e.getStatus());
    }

    @Test
    void setters_ShouldUpdateFields() {
        TaskEntity e = new TaskEntity();
        e.setId("x1");
        e.setStudentId("S2");
        e.setSubjectId("PHYS-200");
        e.setTitle("New Title");
        e.setDescription("New Desc");
        e.setEstimatedDurationMinutes(30);
        e.setDeadline(deadline);
        e.setPriority(TaskPriority.CRITICAL);
        e.setStatus(TaskStatus.COMPLETED);
        e.setScheduledDate(scheduled);
        e.setCompletedAt(deadline);

        assertEquals("x1", e.getId());
        assertEquals("S2", e.getStudentId());
        assertEquals("PHYS-200", e.getSubjectId());
        assertEquals("New Title", e.getTitle());
        assertEquals("New Desc", e.getDescription());
        assertEquals(30, e.getEstimatedDurationMinutes());
        assertEquals(deadline, e.getDeadline());
        assertEquals(TaskPriority.CRITICAL, e.getPriority());
        assertEquals(TaskStatus.COMPLETED, e.getStatus());
        assertEquals(scheduled, e.getScheduledDate());
        assertEquals(deadline, e.getCompletedAt());
    }

    @Test
    void equals_WhenSameFields_ShouldBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void equals_WhenDifferentId_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setId("different");

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenComparedWithSelf_ShouldBeTrue() {
        TaskEntity e = buildEntity();
        assertEquals(e, e);
    }

    @Test
    void equals_WhenComparedWithNull_ShouldBeFalse() {
        TaskEntity e = buildEntity();
        assertNotEquals(null, e);
    }

    @Test
    void equals_WhenDifferentStatus_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setStatus(TaskStatus.COMPLETED);

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentPriority_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setPriority(TaskPriority.LOW);

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentStudentId_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setStudentId("S99");

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentSubjectId_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setSubjectId("OTHER-999");

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentTitle_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setTitle("Different Title");

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentDescription_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setDescription("Different description");

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentDeadline_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setDeadline(deadline.plusDays(10));

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentDuration_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setEstimatedDurationMinutes(999);

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenDifferentScheduledDate_ShouldNotBeEqual() {
        TaskEntity e1 = buildEntity();
        TaskEntity e2 = buildEntity();
        e2.setScheduledDate(scheduled.plusDays(1));

        assertNotEquals(e1, e2);
    }

    @Test
    void equals_WhenNullFieldsMatch_ShouldBeEqual() {
        TaskEntity e1 = new TaskEntity();
        TaskEntity e2 = new TaskEntity();

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        TaskEntity e = buildEntity();
        String str = e.toString();

        assertTrue(str.contains("e1"));
        assertTrue(str.contains("S1"));
        assertTrue(str.contains("Task Title"));
    }
}
