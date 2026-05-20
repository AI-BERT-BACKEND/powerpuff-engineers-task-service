package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.AcademicApiResponse;
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

@Component
@Profile("feign")
@Primary
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceFeignAdapter implements SubjectValidationPort {

    private final AcademicServiceClient academicServiceClient;

    @Override
    public boolean exists(String subjectId, String studentId) {
        try {
            AcademicApiResponse<SubjectDTO> response = academicServiceClient.getSubjectById(studentId, subjectId);
            if (response == null || !response.isSuccess() || response.getData() == null) {
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

    @Override
    public boolean isInActiveSemester(String subjectId, String studentId) {
        return exists(subjectId, studentId);
    }
}
