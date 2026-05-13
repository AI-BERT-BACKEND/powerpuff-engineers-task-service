package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.domain.model.Task;

/**
 * Input port for editing an existing task (R39).
 */
public interface UpdateTaskUseCase {

    /**
     * Applies partial updates to a task.
     * Only non-null fields in {@code request} are applied.
     *
     * <p>Business rules enforced:</p>
     * <ul>
     *   <li>RN-01: only the task owner (matching {@code studentId}) may edit it.</li>
     *   <li>RN-02: tasks with status {@code COMPLETED} cannot be edited.</li>
     *   <li>RN-03: if {@code deadline} changes, priority is recalculated using the 24-hour urgency rule.</li>
     * </ul>
     *
     * @param taskId    the identifier of the task to update
     * @param studentId the identifier of the requesting student (ownership check)
     * @param request   the partial update payload
     * @return the updated and persisted task
     * @throws com.aibert.dosw.domain.exceptions.TaskNotFoundException   if no task exists with the given ID
     * @throws com.aibert.dosw.domain.exceptions.TaskForbiddenException  if the task does not belong to the student
     * @throws com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException if the task is already COMPLETED
     */
    Task updateTask(String taskId, String studentId, UpdateTaskRequest request);
}
