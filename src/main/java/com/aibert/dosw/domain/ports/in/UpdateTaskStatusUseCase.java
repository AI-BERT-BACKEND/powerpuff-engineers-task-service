package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;

/**
 * Inbound port for updating a task's status (R13 - AC2, AC3).
 */
public interface UpdateTaskStatusUseCase {

    /**
     * Updates the status of a task. If the new status is COMPLETED,
     * the completedAt timestamp is automatically set.
     *
     * @param taskId    the unique identifier of the task
     * @param newStatus the new status to apply
     * @return the updated task
     */
    Task updateStatus(String taskId, TaskStatus newStatus);
}
