package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.DeleteTaskUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Spring service implementing the {@link DeleteTaskUseCase} input port (R40).
 * Verifies task existence and student ownership before permanently removing the task.
 */
@Service
@RequiredArgsConstructor
public class DeleteTaskUseCaseImpl implements DeleteTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     * <p>Execution order:</p>
     * <ol>
     *   <li>Loads the task; throws {@link TaskNotFoundException} if absent.</li>
     *   <li>Verifies the requesting student owns the task (RN-01); throws {@link TaskForbiddenException} otherwise.</li>
     *   <li>Permanently deletes the task from the repository.</li>
     * </ol>
     *
     * @param taskId    the identifier of the task to delete
     * @param studentId the identifier of the requesting student
     */
    @Override
    public void deleteTask(String taskId, String studentId) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // RN-01: ownership check
        if (!task.getStudentId().equals(studentId)) {
            throw new TaskForbiddenException(taskId);
        }

        taskRepositoryPort.deleteById(taskId);
    }
}
