package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.TokenValidationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for auth-service (port 8080).
 *
 * Base URL configured in application.yml under:
 *   clients.auth-service.url
 *
 * Used to validate the JWT token on every incoming request to task-service.
 * Fallback is active when feign.circuitbreaker.enabled=true.
 */
@FeignClient(
        name = "auth-service",
        url = "${clients.auth-service.url}",
        fallback = AuthServiceFallback.class
)
public interface AuthServiceClient {

    /**
     * Validates the JWT token and returns the userId and roles of the authenticated user.
     *
     * @param request contains the Authorization header (e.g. "Bearer eyJ...")
     * @return user data if the token is valid; fallback returns an invalid response object
     */
    @PostMapping("/api/auth/validate")
    TokenValidationDTO.Response validateToken(@RequestBody TokenValidationDTO.Request request);
}
