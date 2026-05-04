package com.aibert.dosw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a subject received from academic-service.
 * Used in the responses of the {@code AcademicServiceClient} Feign client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {

    private String id;
    private String name;
    private String code;
    private String teacherName;
    private Integer credits;
}
