package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub local activo cuando el perfil 'feign' NO está presente.
 * Acepta cualquier subjectId no vacío. Útil en tests y desarrollo local.
 */
@Component
@Profile("!feign")
public class SubjectValidationAdapter implements SubjectValidationPort {

    @Override
    public boolean exists(String subjectId) {
        return subjectId != null && !subjectId.isBlank();
    }
}
