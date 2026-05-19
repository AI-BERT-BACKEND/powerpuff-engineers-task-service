package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.request.TaskCompletedEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "gamification-service",
        url = "${clients.gamification-service.url}",
        fallback = GamificationServiceFallback.class
)
public interface GamificationServiceClient {

    @PostMapping("/api/v1/events/task-completed")
    void notifyTaskCompleted(@RequestBody TaskCompletedEventDTO event);
}
