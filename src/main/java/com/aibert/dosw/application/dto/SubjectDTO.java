package com.aibert.dosw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {

    private Long id;
    private String studentId;
    private String subjectName;
    private Integer credits;
    private String teacherName;
    private String semester;
}
