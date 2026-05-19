package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of {@link SubjectValidationPort} active when the {@code feign} profile is NOT present.
 * Accepts any non-null, non-blank subject ID as valid.
 * Intended for local development and automated tests that do not integrate with academic-service.
 */
@Component
@Profile("!feign")
public class SubjectValidationAdapter implements SubjectValidationPort {

    /**
     * {@inheritDoc}
     * Returns {@code true} for any non-null, non-blank subject ID
     * without contacting an external service.
     */
    @Override
    public boolean exists(String subjectId) {
        return subjectId != null && !subjectId.isBlank();
    }

    /**
     * {@inheritDoc}
     * Stub: accepts any non-blank subject/student combination.
     * Real validation requires an enrollment endpoint on academic-service.
     */
    @Override
    public boolean isInActiveSemester(String subjectId, String studentId) {
        return subjectId != null && !subjectId.isBlank();
    }
}
