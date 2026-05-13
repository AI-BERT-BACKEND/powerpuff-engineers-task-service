package com.aibert.dosw.domain.exceptions;

/**
 * Thrown when a task lookup by ID returns no result.
 * Maps to HTTP 404 Not Found via {@code GlobalExceptionHandler}.
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with a message that includes the missing task ID.
     *
     * @param taskId the identifier that could not be found
     */
    public TaskNotFoundException(String taskId) {
        super("Tarea no encontrada con ID: " + taskId);
    }
}
