package com.aibert.dosw.infrastructure.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubjectServiceFallbackTest {

    private SubjectServiceFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new SubjectServiceFallback();
    }

    @Test
    void getSubjectById_ShouldReturnNull() {
        SubjectServiceClient.SubjectResponse result = fallback.getSubjectById("PHYS-202");

        assertNull(result);
    }
}
