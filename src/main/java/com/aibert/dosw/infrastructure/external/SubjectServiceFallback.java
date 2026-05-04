package com.aibert.dosw.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@link SubjectServiceClient}.
 *
 * Invoked when subject-service is unavailable, exceeds the timeout,
 * or the circuit breaker is open.
 * Returns null so the caller (SubjectServiceFeignAdapter) treats the subject as non-existent.
 */
@Component
@Slf4j
public class SubjectServiceFallback implements SubjectServiceClient {

    @Override
    public SubjectServiceClient.SubjectResponse getSubjectById(String subjectId) {
        log.warn("subject-service unavailable when fetching subject '{}'. Returning null.", subjectId);
        return null;
    }
}
