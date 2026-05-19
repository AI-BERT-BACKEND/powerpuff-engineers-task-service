package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.application.dto.request.TaskCompletedEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GamificationServiceFallback implements GamificationServiceClient {

    @Override
    public void notifyTaskCompleted(TaskCompletedEventDTO event) {
        log.warn("gamification-service unavailable. Task-completed event for task '{}' was not sent.",
                event.getTaskId());
    }
}
