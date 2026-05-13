package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.GetTaskByIdUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Spring service implementing the {@link GetTaskByIdUseCase} input port (R41 - task detail).
 * Retrieves a single task by its unique identifier.
 */
@Service
@RequiredArgsConstructor
public class GetTaskByIdUseCaseImpl implements GetTaskByIdUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     *
     * @param taskId the task identifier
     * @return the found task
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @Override
    public Task getTaskById(String taskId) {
        return taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}
