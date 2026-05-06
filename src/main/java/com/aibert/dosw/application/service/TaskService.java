package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.SubjectDTO;
import com.aibert.dosw.infrastructure.external.AcademicServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that uses the AcademicServiceClient Feign client.
 *
 * NOTE: task-service does NOT call auth-service.
 * The API Gateway validates the JWT and forwards the authenticated
 * userId via the X-User-Id header. Read that header in the controller.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final AcademicServiceClient academicServiceClient;

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
}

