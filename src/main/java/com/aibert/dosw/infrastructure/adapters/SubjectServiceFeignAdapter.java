package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.SubjectDTO;
import com.aibert.dosw.domain.exceptions.ExternalServiceUnavailableException;
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

    /**
     * {@inheritDoc}
     * Calls {@code GET /api/v1/subjects/{subjectId}}: 200 → true, 404 → false.
     */
    @Override
    public boolean exists(String subjectId) {
        try {
            SubjectDTO subject = academicServiceClient.getSubjectById(subjectId);
            if (subject == null) {
                log.warn("Fallback activo: academic-service no disponible al consultar subject '{}'.", subjectId);
                throw new ExternalServiceUnavailableException(
                        "El servicio académico no está disponible. Inténtelo más tarde.");
            }
            return true;
        } catch (FeignException.NotFound e) {
            log.debug("academic-service: subject '{}' not found.", subjectId);
            return false;
        } catch (FeignException e) {
            log.error("Error contacting academic-service to validate subject '{}': {}", subjectId, e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #exists(String)} until academic-service exposes an enrollment endpoint.
     * When available, this should call {@code GET /api/v1/students/{studentId}/subjects/{subjectId}/active}.
     */
    @Override
    public boolean isInActiveSemester(String subjectId, String studentId) {
        return exists(subjectId);
    }
}
