package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.AcademicApiResponse;
import com.aibert.dosw.application.dto.SubjectDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AcademicServiceFallbackTest {

    private AcademicServiceFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new AcademicServiceFallback();
    }

    @Test
    void getSubjectById_ShouldReturnNull() {
        AcademicApiResponse<SubjectDTO> result = fallback.getSubjectById("student-1", 1L);

        assertNull(result);
    }
}
