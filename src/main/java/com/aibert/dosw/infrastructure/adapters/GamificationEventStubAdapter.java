package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.out.TaskEventPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of {@link TaskEventPort}.
 * Active when the 'feign' profile is NOT active (e.g. inmemory, tests).
 * Logs the event without making any HTTP call.
 */
@Component
@Profile("!feign")
@Slf4j
public class GamificationEventStubAdapter implements TaskEventPort {

    @Override
    public void notifyTaskCompleted(Task task) {
        log.debug("[STUB] Task-completed event for task '{}' (stub — gamification-service not called).",
                task.getId());
    }
}
