package com.aibert.dosw.domain.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionsTest {

    @Test
    void taskNotFoundException_ShouldContainId() {
        TaskNotFoundException ex = new TaskNotFoundException("task-99");
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("task-99"));
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void taskConflictException_ShouldContainMessage() {
        TaskConflictException ex = new TaskConflictException("Ya existe una tarea con ese título");
        assertEquals("Ya existe una tarea con ese título", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void subjectNotFoundException_ShouldContainMessage() {
        SubjectNotFoundException ex = new SubjectNotFoundException("Materia MATH-101 no encontrada");
        assertEquals("Materia MATH-101 no encontrada", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void taskForbiddenException_ShouldContainTaskId() {
        TaskForbiddenException ex = new TaskForbiddenException("task-77");
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("task-77"));
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void taskEditNotAllowedException_ShouldContainTaskId() {
        TaskEditNotAllowedException ex = new TaskEditNotAllowedException("task-88");
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("task-88"));
        assertInstanceOf(RuntimeException.class, ex);
    }
}
