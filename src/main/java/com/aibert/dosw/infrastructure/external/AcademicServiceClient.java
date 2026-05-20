package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.AcademicApiResponse;
import com.aibert.dosw.application.dto.SubjectDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "academic-service",
        url = "${clients.academic-service.url}",
        fallback = AcademicServiceFallback.class
)
public interface AcademicServiceClient {

    @GetMapping("/api/v1/subjects/{subjectId}")
    AcademicApiResponse<SubjectDTO> getSubjectById(
            @RequestHeader("X-Student-Id") String studentId,
            @PathVariable("subjectId") String subjectId);
}
