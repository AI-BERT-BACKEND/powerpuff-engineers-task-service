package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

/**
 * Input port for the task creation use case.
 * Validates the subject and uniqueness constraints before persisting a new task.
 */
public interface CreateTaskUseCase {

    /**
     * Creates and persists a new task.
     * <p>Validates that the subject exists and that no duplicate task exists for
     * the same student and subject. Assigns {@code MEDIUM} priority and
     * {@code TODO} status when not provided.</p>
     *
     * @param task the task to be created (id, status, and scheduledDate are ignored)
     * @return the persisted task with a generated ID and default field values
     * @throws com.aibert.dosw.domain.exceptions.SubjectNotFoundException if the subject does not exist
     * @throws com.aibert.dosw.domain.exceptions.TaskConflictException    if a duplicate task already exists
     */
    Task createTask(Task task);
}
