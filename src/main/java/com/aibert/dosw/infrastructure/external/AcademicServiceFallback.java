package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.SubjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback for {@link AcademicServiceClient}.
 *
 * Invoked when academic-service is unavailable, exceeds the timeout,
 * or the circuit breaker is open.
 * Returns safe values to avoid interrupting the service flow.
 */
@Component
@Slf4j
public class AcademicServiceFallback implements AcademicServiceClient {

    @Override
    public SubjectDTO getSubjectById(String subjectId) {
        log.warn("academic-service unavailable when fetching subject '{}'. Returning null.", subjectId);
        return null;
    }

    @Override
    public List<SubjectDTO> getSubjectsByUserId(String userId) {
        log.warn("academic-service unavailable when listing subjects for user '{}'. Returning empty list.", userId);
        return Collections.emptyList();
    }
}
