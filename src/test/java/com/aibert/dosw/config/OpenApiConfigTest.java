package com.aibert.dosw.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void customOpenAPI_ShouldReturnNonNullBean() {
        OpenAPI openAPI = config.customOpenAPI();
        assertNotNull(openAPI);
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectTitle() {
        OpenAPI openAPI = config.customOpenAPI();
        assertNotNull(openAPI.getInfo());
        assertEquals("Task Service API", openAPI.getInfo().getTitle());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectVersion() {
        OpenAPI openAPI = config.customOpenAPI();
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void customOpenAPI_ShouldHaveDescription() {
        OpenAPI openAPI = config.customOpenAPI();
        assertNotNull(openAPI.getInfo().getDescription());
    }
}
