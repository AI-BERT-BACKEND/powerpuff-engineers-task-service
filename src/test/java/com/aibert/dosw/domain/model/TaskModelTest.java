package com.aibert.dosw.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskModelTest {

    private final LocalDateTime deadline = LocalDateTime.now().plusDays(5);
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void builder_ShouldCreateTaskWithAllFields() {
        Task task = Task.builder()
                .id("task-1")
                .studentId("student-1")
                .title("Estudiar álgebra")
                .subjectId("MATH-101")
                .description("Revisar capítulos 1 al 5")
                .estimatedDurationMinutes(120)
                .deadline(deadline)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .scheduledDate(now)
                .completedAt(null)
                .build();

        assertEquals("task-1", task.getId());
        assertEquals("student-1", task.getStudentId());
        assertEquals("Estudiar álgebra", task.getTitle());
        assertEquals("MATH-101", task.getSubjectId());
        assertEquals("Revisar capítulos 1 al 5", task.getDescription());
        assertEquals(120, task.getEstimatedDurationMinutes());
        assertEquals(deadline, task.getDeadline());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(now, task.getScheduledDate());
        assertNull(task.getCompletedAt());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyTask() {
        Task task = new Task();
        assertNull(task.getId());
        assertNull(task.getStudentId());
        assertNull(task.getTitle());
    }

    @Test
    void setters_ShouldUpdateFields() {
        Task task = new Task();
        task.setId("id-1");
        task.setStudentId("s-1");
        task.setTitle("Nueva tarea");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.LOW);
        task.setCompletedAt(now);

        assertEquals("id-1", task.getId());
        assertEquals("s-1", task.getStudentId());
        assertEquals("Nueva tarea", task.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertEquals(now, task.getCompletedAt());
    }

    @Test
    void equals_ShouldReturnTrueForSameTasks() {
        Task t1 = Task.builder().id("1").title("A").build();
        Task t2 = Task.builder().id("1").title("A").build();
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toString_ShouldContainFields() {
        Task task = Task.builder().id("x").title("Test").build();
        String str = task.toString();
        assertTrue(str.contains("x"));
        assertTrue(str.contains("Test"));
    }

    @Test
    void taskStatus_ShouldHaveCorrectValues() {
        assertEquals(4, TaskStatus.values().length);
        assertEquals(TaskStatus.TODO, TaskStatus.valueOf("TODO"));
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.valueOf("IN_PROGRESS"));
        assertEquals(TaskStatus.PAUSED, TaskStatus.valueOf("PAUSED"));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.valueOf("COMPLETED"));
    }

    @Test
    void taskPriority_ShouldHaveCorrectValues() {
        assertEquals(4, TaskPriority.values().length);
        assertEquals(TaskPriority.LOW, TaskPriority.valueOf("LOW"));
        assertEquals(TaskPriority.MEDIUM, TaskPriority.valueOf("MEDIUM"));
        assertEquals(TaskPriority.HIGH, TaskPriority.valueOf("HIGH"));
        assertEquals(TaskPriority.CRITICAL, TaskPriority.valueOf("CRITICAL"));
    }

    @Test
    void sortCriteriaEnum_ShouldHaveCorrectValues() {
        assertEquals(3, SortCriteriaEnum.values().length);
        assertEquals(SortCriteriaEnum.PRIORITY, SortCriteriaEnum.valueOf("PRIORITY"));
        assertEquals(SortCriteriaEnum.DEADLINE, SortCriteriaEnum.valueOf("DEADLINE"));
        assertEquals(SortCriteriaEnum.SUBJECT, SortCriteriaEnum.valueOf("SUBJECT"));
    }
}
