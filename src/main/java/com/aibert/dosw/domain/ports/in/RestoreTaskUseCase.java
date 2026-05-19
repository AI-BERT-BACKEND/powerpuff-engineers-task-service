package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

/**
 * Input port for restoring a previously soft-deleted task (R16 — optional "Undo").
 */
public interface RestoreTaskUseCase {

    /**
     * Restores the soft-deleted task identified by {@code taskId}.
     * Only the original owner may restore a task.
     *
     * @param taskId    the identifier of the task to restore
     * @param studentId the identifier of the requesting student (ownership check)
     * @return the restored {@link Task}
     * @throws com.aibert.dosw.domain.exceptions.TaskNotFoundException  if no deleted task exists with the given ID
     * @throws com.aibert.dosw.domain.exceptions.TaskForbiddenException if the task does not belong to the student
     */
    Task restoreTask(String taskId, String studentId);
}
