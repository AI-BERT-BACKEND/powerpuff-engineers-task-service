package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.SubjectDTO;
import com.aibert.dosw.application.dto.TokenValidationDTO;
import com.aibert.dosw.infrastructure.external.AcademicServiceClient;
import com.aibert.dosw.infrastructure.external.AuthServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Example of Feign client usage in task-service.
 *
 * Shows how to call academic-service (to validate/list subjects)
 * and auth-service (to validate the JWT token before processing a request).
 *
 * In production this service would be integrated with the use cases (usecase/).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final AcademicServiceClient academicServiceClient;
    private final AuthServiceClient authServiceClient;

    /**
     * Returns true if the subject exists in academic-service.
     * If academic-service is unreachable, the fallback returns null and it is treated as non-existent.
     */
    public boolean validateSubjectExists(String subjectId) {
        try {
            SubjectDTO subject = academicServiceClient.getSubjectById(subjectId);
            if (subject == null) {
                log.warn("Could not confirm existence of subject '{}' (fallback active).", subjectId);
                return false;
            }
            log.debug("Subject '{}' validated: {}", subjectId, subject.getName());
            return true;
        } catch (FeignException.NotFound e) {
            log.debug("Subject '{}' does not exist in academic-service.", subjectId);
            return false;
        }
    }

    /**
     * Retrieves all subjects for a user from academic-service.
     * If the service is unavailable, the fallback returns an empty list.
     */
    public List<SubjectDTO> getAvailableSubjectsForUser(String userId) {
        List<SubjectDTO> subjects = academicServiceClient.getSubjectsByUserId(userId);
        log.debug("Subjects retrieved for user '{}': {} result(s).", userId, subjects.size());
        return subjects;
    }

    /**
     * Validates the Authorization header against auth-service.
     *
     * @param authorizationHeader full header value, e.g. "Bearer eyJ..."
     * @return response with userId and roles if the token is valid;
     *         response with valid=false if the service is unavailable (fallback)
     */
    public TokenValidationDTO.Response validateToken(String authorizationHeader) {
        TokenValidationDTO.Request request = TokenValidationDTO.Request.builder()
                .authorization(authorizationHeader)
                .build();

        TokenValidationDTO.Response response = authServiceClient.validateToken(request);

        if (!response.isValid()) {
            log.warn("Invalid token or auth-service unavailable.");
        } else {
            log.debug("Valid token for userId='{}', roles={}.", response.getUserId(), response.getRoles());
        }

        return response;
    }
}
