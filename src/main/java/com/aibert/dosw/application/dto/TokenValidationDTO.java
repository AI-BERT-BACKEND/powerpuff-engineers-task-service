package com.aibert.dosw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs for JWT token validation against auth-service.
 */
public final class TokenValidationDTO {

    private TokenValidationDTO() {}

    /**
     * Request sent to POST /api/auth/validate.
     * Contains the full Authorization header (e.g. "Bearer eyJ...").
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String authorization;
    }

    /**
     * Response returned by auth-service when the token is valid.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String userId;
        private List<String> roles;
        private boolean valid;
    }
}
