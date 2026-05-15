package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
import com.aibert.dosw.domain.ports.out.TaskEventPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring service implementing the {@link UpdateTaskStatusUseCase} input port (R42).
 * Enforces ownership, manages the {@code completedAt} timestamp, and recalculates
 * urgency-based priority for all remaining active tasks after any status change.
 */
@Service
@RequiredArgsConstructor
public class UpdateTaskStatusUseCaseImpl implements UpdateTaskStatusUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final TaskEventPort taskEventPort;

    private static final int DEADLINE_URGENCY_HOURS = 24;

    /**
     * {@inheritDoc}
     * <p>Execution order:</p>
     * <ol>
     *   <li>Loads the task; throws {@link TaskNotFoundException} if absent.</li>
     *   <li>Verifies the requesting student owns the task (RN-01); throws {@link TaskForbiddenException} otherwise.</li>
     *   <li>Sets {@code completedAt} when moving to {@code COMPLETED} (RN-03); clears it otherwise.</li>
     *   <li>Persists the updated task.</li>
     *   <li>Recalculates urgency-based priority for all remaining active tasks of the student (RN-04).</li>
     * </ol>
     *
     * @param taskId    the ID of the task to update
     * @param studentId the identifier of the requesting student
     * @param newStatus the target status
     * @return the updated and persisted task
     */
    @Override
    public Task updateStatus(String taskId, String studentId, TaskStatus newStatus) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // RN-01: only the owner may change the status
        if (!task.getStudentId().equals(studentId)) {
            throw new TaskForbiddenException(taskId);
        }

        task.setStatus(newStatus);

        // RN-03: record or clear completedAt based on the target status
        if (TaskStatus.COMPLETED.equals(newStatus)) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        Task saved = taskRepositoryPort.save(task);

        // Notify gamification-service when a task is completed
        if (TaskStatus.COMPLETED.equals(newStatus)) {
            taskEventPort.notifyTaskCompleted(saved);
        }

        // RN-04: recalculate urgency-based priority for remaining active tasks
        recalculatePriorityForActiveTasks(studentId);

        return saved;
    }

    /**
     * Escalates to {@code HIGH} any active (non-COMPLETED) task whose deadline falls within
     * the next {@value DEADLINE_URGENCY_HOURS} hours, provided it is not already {@code HIGH}
     * or {@code CRITICAL}. Persists updated tasks in bulk if any change occurred.
     *
     * @param studentId the student whose active tasks will be re-evaluated
     */
    private void recalculatePriorityForActiveTasks(String studentId) {
        List<Task> activeTasks = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        boolean anyUpdated = false;

        for (Task t : activeTasks) {
            if (t.getDeadline() == null) continue;
            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, t.getDeadline());
            boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
            boolean canEscalate = t.getPriority() != TaskPriority.HIGH
                    && t.getPriority() != TaskPriority.CRITICAL;
            if (isUrgent && canEscalate) {
                t.setPriority(TaskPriority.HIGH);
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            taskRepositoryPort.saveAll(activeTasks);
        }
    }
}

