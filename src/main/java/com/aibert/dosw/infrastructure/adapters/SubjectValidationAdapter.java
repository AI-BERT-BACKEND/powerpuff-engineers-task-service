package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import org.springframework.stereotype.Component;

@Component
public class SubjectValidationAdapter implements SubjectValidationPort {

    @Override
    public boolean exists(String subjectId) {
        return subjectId != null && !subjectId.isBlank();
    }
}
