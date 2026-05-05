package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.TokenValidationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceFallbackTest {

    private AuthServiceFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new AuthServiceFallback();
    }

    @Test
    void validateToken_ShouldReturnInvalidResponse() {
        TokenValidationDTO.Request request = TokenValidationDTO.Request.builder()
                .authorization("Bearer some.token.here")
                .build();

        TokenValidationDTO.Response response = fallback.validateToken(request);

        assertNotNull(response);
        assertFalse(response.isValid());
        assertNull(response.getUserId());
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().isEmpty());
    }
}
