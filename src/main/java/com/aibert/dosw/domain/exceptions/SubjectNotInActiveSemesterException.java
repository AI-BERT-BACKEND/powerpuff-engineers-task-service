package com.aibert.dosw.domain.exceptions;

/**
 * Thrown when the subject exists but does not belong to the student's active semester (AIB-18.1 FA-03).
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class SubjectNotInActiveSemesterException extends RuntimeException {

    public SubjectNotInActiveSemesterException(String subjectId) {
        super("La materia '" + subjectId + "' no pertenece al semestre académico activo del estudiante.");
    }
}
