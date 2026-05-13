package com.aibert.dosw.domain.exceptions;

/**
 * Thrown when attempting to create a task that would violate a uniqueness
 * constraint (same student, subject, and title already exist).
 * Maps to HTTP 409 Conflict via {@code GlobalExceptionHandler}.
 */
public class TaskConflictException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive conflict message.
     *
     * @param message human-readable description of the conflict
     */
    public TaskConflictException(String message) {
        super(message);
    }
}
