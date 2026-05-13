package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.SubjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AcademicServiceFallback implements AcademicServiceClient {

    /**
     * Returns {@code null} as a safe fallback when academic-service is unavailable.
     * The caller ({@link com.aibert.dosw.application.service.TaskService}) interprets
     * a {@code null} result as an unconfirmed (non-existent) subject.
     *
     * @param subjectId the subject identifier that could not be retrieved
     * @return {@code null}
     */
    @Override
    public SubjectDTO getSubjectById(String subjectId) {
        log.warn("academic-service unavailable when fetching subject '{}'. Returning null.", subjectId);
        return null;
    }
}
