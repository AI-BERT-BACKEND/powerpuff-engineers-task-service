package com.aibert.dosw.domain.exceptions;

/**
 * Thrown when an attempt is made to edit a task whose status is {@code COMPLETED}.
 * Maps to HTTP 400 Bad Request via {@code GlobalExceptionHandler}.
 */
public class TaskEditNotAllowedException extends RuntimeException {

    /**
     * Constructs the exception with the offending task ID.
     *
     * @param taskId the identifier of the completed task that cannot be modified
     */
    public TaskEditNotAllowedException(String taskId) {
        super("No se permite editar una tarea completada. ID: " + taskId);
    }
}
