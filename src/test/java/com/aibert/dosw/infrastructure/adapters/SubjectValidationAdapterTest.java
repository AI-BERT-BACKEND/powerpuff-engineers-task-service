package com.aibert.dosw.infrastructure.adapters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubjectValidationAdapterTest {

    private SubjectValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubjectValidationAdapter();
    }

    @Test
    void exists_WhenSubjectIdIsValid_ShouldReturnTrue() {
        assertTrue(adapter.exists("MATH-101"));
    }

    @Test
    void exists_WhenSubjectIdIsNull_ShouldReturnFalse() {
        assertFalse(adapter.exists(null));
    }

    @Test
    void exists_WhenSubjectIdIsBlank_ShouldReturnFalse() {
        assertFalse(adapter.exists("   "));
    }

    @Test
    void exists_WhenSubjectIdIsEmpty_ShouldReturnFalse() {
        assertFalse(adapter.exists(""));
    }

    @Test
    void exists_WhenSubjectIdHasSpaces_ShouldReturnTrue() {
        assertTrue(adapter.exists("MATH 101"));
    }
}
