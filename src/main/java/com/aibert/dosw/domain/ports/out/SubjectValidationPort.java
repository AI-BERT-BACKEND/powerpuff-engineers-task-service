package com.aibert.dosw.domain.ports.out;

public interface SubjectValidationPort {
    boolean exists(String subjectId);
}
