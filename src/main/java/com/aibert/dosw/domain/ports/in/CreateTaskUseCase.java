package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

/**
 * Input port in charge of handling the creation of new tasks.
 */
public interface CreateTaskUseCase {
    
    /**
     * Creates a new task and persists it.
     * Ensures that the task has an initial status of TODO if none is provided
     * to avoid state inconsistencies.
     *
     * @param task the domain task to be created
     * @return the successfully created and persisted task
     */
    Task createTask(Task task);
}
