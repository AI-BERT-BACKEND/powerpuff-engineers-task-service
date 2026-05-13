package com.aibert.dosw.domain.ports.in;

/**
 * Input port for permanently deleting a task (R40).
 */
public interface DeleteTaskUseCase {

    /**
     * Deletes the task identified by {@code taskId} after verifying ownership.
     *
     * <p>Business rules enforced:</p>
     * <ul>
     *   <li>RN-01: only the task owner (matching {@code studentId}) may delete it.</li>
     * </ul>
     *
     * @param taskId    the identifier of the task to delete
     * @param studentId the identifier of the requesting student (ownership check)
     * @throws com.aibert.dosw.domain.exceptions.TaskNotFoundException  if no task exists with the given ID
     * @throws com.aibert.dosw.domain.exceptions.TaskForbiddenException if the task does not belong to the student
     */
    void deleteTask(String taskId, String studentId);
}
