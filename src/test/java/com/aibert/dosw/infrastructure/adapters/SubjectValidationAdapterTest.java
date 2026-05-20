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
        assertTrue(adapter.exists("MATH-101", "student-1"));
    }

    @Test
    void exists_WhenSubjectIdIsNull_ShouldReturnFalse() {
        assertFalse(adapter.exists(null, "student-1"));
    }

    @Test
    void exists_WhenSubjectIdIsBlank_ShouldReturnFalse() {
        assertFalse(adapter.exists("   ", "student-1"));
    }

    @Test
    void exists_WhenSubjectIdIsEmpty_ShouldReturnFalse() {
        assertFalse(adapter.exists("", "student-1"));
    }

    @Test
    void exists_WhenSubjectIdHasSpaces_ShouldReturnTrue() {
        assertTrue(adapter.exists("MATH 101", "student-1"));
    }

    @Test
    void isInActiveSemester_WhenSubjectIdIsValid_ShouldReturnTrue() {
        assertTrue(adapter.isInActiveSemester("MATH-101", "student-1"));
    }

    @Test
    void isInActiveSemester_WhenSubjectIdIsNull_ShouldReturnFalse() {
        assertFalse(adapter.isInActiveSemester(null, "student-1"));
    }

    @Test
    void isInActiveSemester_WhenSubjectIdIsBlank_ShouldReturnFalse() {
        assertFalse(adapter.isInActiveSemester("   ", "student-1"));
    }
}
