package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.request.TaskCompletedEventDTO;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.out.TaskEventPort;
import com.aibert.dosw.infrastructure.external.GamificationServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Real implementation of {@link TaskEventPort} via Feign.
 * Active only with the 'feign' profile.
 * Sends a task-completed event to gamification-service.
 * Feign errors are logged and swallowed — gamification is non-critical.
 */
@Component
@Profile("feign")
@Primary
@RequiredArgsConstructor
@Slf4j
public class GamificationServiceFeignAdapter implements TaskEventPort {

    private final GamificationServiceClient gamificationServiceClient;

    @Override
    public void notifyTaskCompleted(Task task) {
        TaskCompletedEventDTO event = TaskCompletedEventDTO.builder()
                .taskId(task.getId())
                .studentId(task.getStudentId())
                .subjectId(task.getSubjectId())
                .taskType(task.getTaskType() != null ? task.getTaskType().name() : null)
                .build();
        try {
            gamificationServiceClient.notifyTaskCompleted(event);
            log.debug("Task-completed event sent to gamification-service for task '{}'.", task.getId());
        } catch (FeignException e) {
            log.error("Failed to notify gamification-service for task '{}': {}", task.getId(), e.getMessage());
        }
    }
}
