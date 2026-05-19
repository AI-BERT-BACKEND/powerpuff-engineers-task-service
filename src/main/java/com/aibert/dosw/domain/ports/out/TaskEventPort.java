package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.Task;

/**
 * Output port for publishing task lifecycle events to external services.
 * Implemented by {@code GamificationEventStubAdapter} (stub) and
 * {@code GamificationServiceFeignAdapter} (Feign/real).
 */
public interface TaskEventPort {

    /**
     * Notifies that a task has been completed by a student.
     * Used to trigger gamification logic (points, badges, streaks).
     *
     * @param task the task that was just marked as COMPLETED
     */
    void notifyTaskCompleted(Task task);
}
