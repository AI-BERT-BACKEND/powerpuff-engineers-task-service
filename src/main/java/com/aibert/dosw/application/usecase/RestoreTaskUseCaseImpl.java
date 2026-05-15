package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.RestoreTaskUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Spring service implementing the {@link RestoreTaskUseCase} input port (R16 — optional "Undo").
 * Clears the {@code deletedAt} timestamp on a previously soft-deleted task after verifying ownership.
 */
@Service
@RequiredArgsConstructor
public class RestoreTaskUseCaseImpl implements RestoreTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     * <p>Execution order:</p>
     * <ol>
     *   <li>Calls {@code restore(taskId)} on the repository; returns empty if the task does not exist
     *       or was not deleted → throws {@link TaskNotFoundException}.</li>
     *   <li>Verifies ownership (RN-01); re-soft-deletes and throws {@link TaskForbiddenException}
     *       if the requesting student is not the owner.</li>
     * </ol>
     */
    @Override
    public Task restoreTask(String taskId, String studentId) {
        Task task = taskRepositoryPort.restore(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // RN-01: ownership check — undo the restore if ownership fails
        if (!task.getStudentId().equals(studentId)) {
            taskRepositoryPort.softDelete(taskId);
            throw new TaskForbiddenException(taskId);
        }

        return task;
    }
}
