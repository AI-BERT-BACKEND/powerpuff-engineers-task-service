package com.aibert.dosw.infrastructure.external;

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
        var result = fallback.getSubjectById("student-1", "MATH-101");

        assertNull(result);
    }
}
