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
}
