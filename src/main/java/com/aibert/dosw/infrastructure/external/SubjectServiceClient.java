package com.aibert.dosw.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for subject-service.
 *
 * Base URL configured in application.yml under:
 *   clients.subject-service.url
 *
 * Requires the 'feign' profile to be active (replaces the SubjectValidationAdapter stub).
 */
@FeignClient(name = "subject-service", url = "${clients.subject-service.url}", fallback = SubjectServiceFallback.class)
public interface SubjectServiceClient {

    /**
     * Checks whether a subject exists in subject-service.
     * Expects a 200 response if found, and 404 (FeignException.NotFound) if not.
     */
    @GetMapping("/api/subjects/{subjectId}")
    SubjectResponse getSubjectById(@PathVariable("subjectId") String subjectId);

    record SubjectResponse(String id, String name) {}
}
