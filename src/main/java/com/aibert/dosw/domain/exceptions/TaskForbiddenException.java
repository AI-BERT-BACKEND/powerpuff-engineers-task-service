package com.aibert.dosw.domain.exceptions;

/**
 * Thrown when a student attempts to edit a task that does not belong to them.
 * Maps to HTTP 403 Forbidden via {@code GlobalExceptionHandler}.
 */
public class TaskForbiddenException extends RuntimeException {

    /**
     * Constructs the exception with the offending task ID.
     *
     * @param taskId the identifier of the task the student is not authorized to modify
     */
    public TaskForbiddenException(String taskId) {
        super("No tienes permiso para modificar la tarea con ID: " + taskId);
    }
}
