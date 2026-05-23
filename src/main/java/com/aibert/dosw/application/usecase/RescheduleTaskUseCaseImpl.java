package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.RescheduleTaskUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring service implementing the {@link RescheduleTaskUseCase} input port (R17).
 * Designed for drag-and-drop calendar interactions: updates only {@code scheduledDate}
 * while enforcing ownership, deadline bounds, and time-block overlap rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RescheduleTaskUseCaseImpl implements RescheduleTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     * <p>Execution order:</p>
     * <ol>
     *   <li>Loads the task; throws {@link TaskNotFoundException} if absent.</li>
     *   <li>Verifies ownership (RN-01); throws {@link TaskForbiddenException} otherwise.</li>
     *   <li>Validates that {@code newScheduledDate} does not exceed the task's deadline (RN-R17-01).</li>
     *   <li>Checks for time-block overlap with other scheduled tasks (RN-R17-02).</li>
     *   <li>Persists the updated {@code scheduledDate} and returns the saved task.</li>
     * </ol>
     */
    @Override
    public Task rescheduleTask(String taskId, String studentId, LocalDateTime newScheduledDate) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // RN-01: only the owner may reschedule
        if (!task.getStudentId().equals(studentId)) {
            throw new TaskForbiddenException(taskId);
        }

        // RN-R17-01: scheduledDate must not be after the deadline
        if (task.getDeadline() != null && newScheduledDate.isAfter(task.getDeadline())) {
            throw new TaskConflictException(
                    "La fecha programada no puede ser posterior al deadline de la tarea. "
                    + "Deadline: " + task.getDeadline());
        }

        // RN-R17-02: the new time block must not overlap with another task
        checkOverlap(taskId, studentId, newScheduledDate, task.getEstimatedDurationMinutes());

        task.setScheduledDate(newScheduledDate);
        Task saved = taskRepositoryPort.save(task);
        log.info("AUDIT | operation=RESCHEDULE | studentId={} | taskId={} | newScheduledDate={} | deadline={}",
                studentId, taskId, newScheduledDate, task.getDeadline());
        return saved;
    }

    /**
     * Throws {@link TaskConflictException} if the proposed time block
     * {@code [newStart, newStart + duration]} overlaps with any other scheduled task
     * of the same student. The task being rescheduled is excluded from the check.
     */
    private void checkOverlap(String taskId, String studentId,
                               LocalDateTime newStart, Integer durationMinutes) {
        int duration = durationMinutes != null ? durationMinutes : 0;
        LocalDateTime newEnd = newStart.plusMinutes(duration);

        List<Task> siblings = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> !t.getId().equals(taskId))
                .filter(t -> t.getScheduledDate() != null)
                .toList();

        for (Task other : siblings) {
            int otherDuration = other.getEstimatedDurationMinutes() != null
                    ? other.getEstimatedDurationMinutes() : 0;
            LocalDateTime otherEnd = other.getScheduledDate().plusMinutes(otherDuration);

            boolean overlaps = newStart.isBefore(otherEnd) && newEnd.isAfter(other.getScheduledDate());
            if (overlaps) {
                // Suggest the earliest slot after the last conflicting block ends
                LocalDateTime suggestion = siblings.stream()
                        .filter(t -> t.getScheduledDate() != null)
                        .map(t -> t.getScheduledDate().plusMinutes(
                                t.getEstimatedDurationMinutes() != null ? t.getEstimatedDurationMinutes() : 0))
                        .max(LocalDateTime::compareTo)
                        .orElse(newStart);
                throw new TaskConflictException(
                        "La fecha programada solapa con la tarea '" + other.getTitle()
                        + "'. Fecha sugerida: " + suggestion);
            }
        }
    }
}
