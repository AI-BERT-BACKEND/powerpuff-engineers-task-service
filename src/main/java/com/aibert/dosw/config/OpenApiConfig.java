package com.aibert.dosw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AIbert — Task Service API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing academic tasks within the AIbert student productivity platform.

                                **Capabilities**
                                - Create, read, update and permanently delete tasks.
                                - Automatic priority calculation based on deadline proximity (escalates to CRITICAL within 24 hours).
                                - Three task views: sorted list, Kanban board (Pending / In Progress / Completed), and interactive calendar.
                                - Status transitions with completion timestamp tracking.
                                - Calendar drag-and-drop: move a task's deadline or reschedule its work session.
                                - Overlap detection for scheduled time blocks.
                                - Prioritized active-task list with result caching.
                                - Daily progress summary (completion percentage, scheduled hours, counts).

                                **Task statuses:** `TODO` · `IN_PROGRESS` · `PAUSED` · `COMPLETED`

                                **Task priorities:** `LOW` · `MEDIUM` · `HIGH` · `CRITICAL`
                                """)
                        .contact(new Contact()
                                .name("AIbert Backend Team")
                                .email("ai-bert-backend@powerpuff-engineers.dev")));
    }
}
