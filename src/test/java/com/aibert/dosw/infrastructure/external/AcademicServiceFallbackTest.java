package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.SubjectDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcademicServiceFallbackTest {

    private AcademicServiceFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new AcademicServiceFallback();
    }

    @Test
    void getSubjectById_ShouldReturnNull() {
        SubjectDTO result = fallback.getSubjectById("MATH-101");

        assertNull(result);
    }

    @Test
    void getSubjectsByUserId_ShouldReturnEmptyList() {
        List<SubjectDTO> result = fallback.getSubjectsByUserId("user-1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
