package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SubjectValidationAdapter implements SubjectValidationPort {

    // Dummy valid subjects for demonstration purposes.
    // In a real scenario, this would likely make an HTTP call via FeignClient to a SubjectService.
    private static final Set<String> VALID_SUBJECTS = Set.of("MATH-101", "PHYS-101", "ENG-101", "CS-101");

    @Override
    public boolean exists(String subjectId) {
        if (subjectId == null) return false;
        return VALID_SUBJECTS.contains(subjectId.toUpperCase());
    }
}
