package com.aibert.dosw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Task Service Spring Boot application.
 * Enables component scanning, auto-configuration, and Feign client registration.
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class TaskServiceApplication {

    /**
     * Bootstraps the Spring application context.
     *
     * @param args command-line arguments passed to the JVM (forwarded to Spring)
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
