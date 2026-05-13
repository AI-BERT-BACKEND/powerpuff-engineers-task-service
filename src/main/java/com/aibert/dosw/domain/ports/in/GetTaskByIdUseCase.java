package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

/**
 * Input port for retrieving a single task by its identifier (R41 - task detail on click).
 */
public interface GetTaskByIdUseCase {

    /**
     * Returns the task with the given identifier.
     *
     * @param taskId the task identifier
     * @return the matching task
     * @throws com.aibert.dosw.domain.exceptions.TaskNotFoundException if no task exists with the given ID
     */
    Task getTaskById(String taskId);
}
