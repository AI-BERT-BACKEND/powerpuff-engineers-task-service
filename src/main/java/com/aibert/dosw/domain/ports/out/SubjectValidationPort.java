package com.aibert.dosw.domain.ports.out;

/**
 * Output port for verifying that an academic subject exists before assigning it to a task.
 * Implemented by {@code SubjectValidationAdapter} (stub) and
 * {@code SubjectServiceFeignAdapter} (Feign/real).
 */
public interface SubjectValidationPort {

    /**
     * Returns {@code true} if the subject identified by {@code subjectId} exists.
     *
     * @param subjectId the identifier of the subject to verify
     * @return {@code true} if the subject exists; {@code false} otherwise
     */
    boolean exists(String subjectId);

    /**
     * Returns {@code true} if the subject belongs to the student's current active semester.
     * Used to enforce AIB-18.1 FA-03 (subject must be from the active semester).
     *
     * @param subjectId the identifier of the subject
     * @param studentId the identifier of the student
     * @return {@code true} if the subject is in the student's active semester; {@code false} otherwise
     */
    boolean isInActiveSemester(String subjectId, String studentId);
}
