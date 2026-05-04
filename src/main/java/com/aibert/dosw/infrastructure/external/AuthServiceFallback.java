package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.TokenValidationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Fallback for {@link AuthServiceClient}.
 *
 * Invoked when auth-service is unavailable or exceeds the timeout.
 * Returns a response marked as invalid so the security layer rejects
 * the request with 401 / 403 instead of throwing an unhandled exception.
 */
@Component
@Slf4j
public class AuthServiceFallback implements AuthServiceClient {

    @Override
    public TokenValidationDTO.Response validateToken(TokenValidationDTO.Request request) {
        log.warn("auth-service unavailable for token validation. Returning invalid response.");
        return TokenValidationDTO.Response.builder()
                .valid(false)
                .userId(null)
                .roles(Collections.emptyList())
                .build();
    }
}
