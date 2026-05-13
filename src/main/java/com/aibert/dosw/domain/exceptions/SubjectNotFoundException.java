package com.aibert.dosw.domain.exceptions;

/**
 * Thrown when the academic-service confirms that the referenced subject does not exist.
 * Maps to HTTP 404 Not Found via {@code GlobalExceptionHandler}.
 */
public class SubjectNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message about the missing subject.
     *
     * @param message human-readable description of why the subject was not found
     */
    public SubjectNotFoundException(String message) {
        super(message);
    }
}
