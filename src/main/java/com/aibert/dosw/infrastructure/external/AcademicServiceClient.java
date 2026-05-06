package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.SubjectDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for academic-service (port 8082).
 *
 * Base URL configured in application.yml under:
 *   clients.academic-service.url
 *
 * Fallback is active when feign.circuitbreaker.enabled=true
 * and the remote service is unavailable or returns an error.
 */
@FeignClient(
        name = "academic-service",
        url = "${clients.academic-service.url}",
        fallback = AcademicServiceFallback.class
)
public interface AcademicServiceClient {

    /**
     * Checks that a subject exists before assigning it to a task.
     * Returns 200 if found; Feign throws FeignException.NotFound (404) if not.
     *
     * @param subjectId subject identifier
     * @return subject data
     */
    @GetMapping("/api/subjects/{subjectId}")
    SubjectDTO getSubjectById(@PathVariable("subjectId") String subjectId);

    /**
     * Lists all subjects for a user, used to populate the task creation form.
     *
     * @param userId user identifier
     * @return list of the user's subjects (may be empty)
     */
    @GetMapping("/api/subjects/user/{userId}")
    List<SubjectDTO> getSubjectsByUserId(@PathVariable("userId") String userId);
}
