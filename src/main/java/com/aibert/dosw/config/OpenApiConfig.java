package com.aibert.dosw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring configuration class that defines the OpenAPI (Swagger) specification for the Task Service.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private int serverPort;

    /**
     * Creates and configures the {@link OpenAPI} bean used by SpringDoc
     * to generate the Swagger UI and OpenAPI JSON/YAML documents.
     *
     * @return an {@link OpenAPI} instance with the service title, version, and local server URL
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Service API")
                        .version("1.0.0")
                        .description("API de gestión de tareas para estudiantes"))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local")
                ));
    }
}
