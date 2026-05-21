package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.AcademicApiResponse;
import com.aibert.dosw.application.dto.SubjectDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AcademicServiceFallbackFactory implements FallbackFactory<AcademicServiceClient> {

    @Override
    public AcademicServiceClient create(Throwable cause) {
        return (studentId, subjectId) -> {
            if (cause instanceof FeignException.NotFound || cause instanceof FeignException.Forbidden) {
                throw (FeignException) cause;
            }
            log.warn("academic-service unavailable fetching subject '{}' for student '{}': {}",
                    subjectId, studentId, cause.getMessage());
            return null;
        };
    }
}
