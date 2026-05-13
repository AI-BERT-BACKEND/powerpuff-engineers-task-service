package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;

/**
 * Input port for updating the lifecycle status of an existing task (R42).
 */
public interface UpdateTaskStatusUseCase {

    /**
     * Updates the status of the task identified by {@code taskId}.
     * <p>Only the student who owns the task may change its status (RN-01).
     * When the new status is {@code COMPLETED}, {@code completedAt} is set to the current
     * timestamp (RN-03). For any other status, {@code completedAt} is cleared.
     * After persisting, the urgency-based priority is recalculated for all remaining
     * active tasks of the student (RN-04).</p>
     *
     * @param taskId    the identifier of the task to update
     * @param studentId the identifier of the requesting student
     * @param newStatus the new status to apply
     * @return the updated and persisted task
     * @throws com.aibert.dosw.domain.exceptions.TaskNotFoundException    if no task exists with the given ID
     * @throws com.aibert.dosw.domain.exceptions.TaskForbiddenException   if the student does not own the task
     */
    Task updateStatus(String taskId, String studentId, TaskStatus newStatus);
}
