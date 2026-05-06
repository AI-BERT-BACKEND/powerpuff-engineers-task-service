package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.infrastructure.external.AcademicServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Real implementation of SubjectValidationPort via Feign.
 * Active only with the 'feign' profile.
 *
 * academic-service returns 200  → subject exists  → true
 * academic-service returns 404  → subject not found → false
 * Any other network/server error → propagated as exception
 *   so the caller (CreateTaskUseCaseImpl) can handle it.
 */
@Component
@Profile("feign")
@Primary
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceFeignAdapter implements SubjectValidationPort {

    private final AcademicServiceClient academicServiceClient;

    @Override
    public boolean exists(String subjectId) {
        try {
            academicServiceClient.getSubjectById(subjectId);
            return true;
        } catch (FeignException.NotFound e) {
            log.debug("academic-service: subject '{}' not found.", subjectId);
            return false;
        } catch (FeignException e) {
            log.error("Error contacting academic-service to validate subject '{}': {}", subjectId, e.getMessage());
            throw e;
        }
    }
}
