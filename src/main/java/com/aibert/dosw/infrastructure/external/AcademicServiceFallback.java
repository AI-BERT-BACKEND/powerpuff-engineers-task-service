package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.AcademicApiResponse;
import com.aibert.dosw.application.dto.SubjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AcademicServiceFallback implements AcademicServiceClient {

    @Override
    public AcademicApiResponse<SubjectDTO> getSubjectById(String studentId, String subjectId) {
        log.warn("academic-service unavailable when fetching subject '{}'. Returning null.", subjectId);
        return null;
    }
}
