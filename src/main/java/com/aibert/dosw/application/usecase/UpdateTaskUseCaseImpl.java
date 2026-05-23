package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.UpdateTaskUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Spring service implementing the {@link UpdateTaskUseCase} input port (R39).
 * Applies partial field updates to an existing task while enforcing ownership,
 * completion-lock, and deadline-driven priority recalculation rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTaskUseCaseImpl implements UpdateTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    private static final int DEADLINE_URGENCY_HOURS = 24;

    /**
     * {@inheritDoc}
     * <p>Execution order:</p>
     * <ol>
     *   <li>Loads the task; throws {@link TaskNotFoundException} if absent.</li>
     *   <li>Verifies the requesting student owns the task (RN-01); throws {@link TaskForbiddenException} otherwise.</li>
     *   <li>Rejects edits on COMPLETED tasks (RN-02); throws {@link TaskEditNotAllowedException}.</li>
     *   <li>Applies each non-null field from the request.</li>
     *   <li>If the deadline changed (RN-03), recalculates priority using the 24-hour urgency rule.</li>
     *   <li>Persists and returns the updated task.</li>
     * </ol>
     *
     * @param taskId    the identifier of the task to update
     * @param studentId the identifier of the requesting student
     * @param request   the partial update payload
     * @return the updated and persisted task
     */
    @Override
    public Task updateTask(String taskId, String studentId, UpdateTaskRequest request) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // RN-01: ownership check
        if (!task.getStudentId().equals(studentId)) {
            throw new TaskForbiddenException(taskId);
        }

        // RN-02: completed tasks cannot be edited
        if (TaskStatus.COMPLETED.equals(task.getStatus())) {
            throw new TaskEditNotAllowedException(taskId);
        }

        // Capture original values to detect changes for RN-03
        LocalDateTime originalDeadline = task.getDeadline();
        TaskPriority originalPriority = task.getPriority();
        boolean priorityExplicitlySet = request.getPriority() != null;

        // Apply partial updates
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getSubjectId() != null && !request.getSubjectId().isBlank()) {
            task.setSubjectId(request.getSubjectId());
        }
        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getEstimatedDurationMinutes() != null) {
            task.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        }
        if (request.getScheduledDate() != null) {
            checkScheduledDateConflict(task.getId(), task.getStudentId(),
                    request.getScheduledDate(),
                    task.getEstimatedDurationMinutes());
            task.setScheduledDate(request.getScheduledDate());
        }

        // RN-03: recalculate priority if deadline changed and user did NOT explicitly set a priority
        boolean deadlineChanged = request.getDeadline() != null
                && !request.getDeadline().equals(originalDeadline);
        if (!priorityExplicitlySet && deadlineChanged && task.getDeadline() != null) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), task.getDeadline());
            boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
            boolean canEscalate = task.getPriority() != TaskPriority.HIGH
                    && task.getPriority() != TaskPriority.CRITICAL;
            if (isUrgent && canEscalate) {
                task.setPriority(TaskPriority.HIGH);
            }
        }

        boolean deadlineChanged = request.getDeadline() != null && !request.getDeadline().equals(originalDeadline);
        boolean priorityChanged = task.getPriority() != originalPriority;

        Task saved = taskRepositoryPort.save(task);
        log.info("AUDIT | operation=EDIT | studentId={} | taskId={} | deadlineChanged={} | priorityChanged={} | priorityEscalated={} | newPriority={} | newDeadline={}",
                studentId, taskId, deadlineChanged, priorityChanged,
                (priorityChanged && !priorityExplicitlySet),
                saved.getPriority(), saved.getDeadline());
        return saved;
    }

    /**
     * Validates that the requested {@code scheduledDate} does not overlap with any other
     * active task of the same student. Overlap is determined using the task's
     * {@code estimatedDurationMinutes} as the time window.
     * Throws {@link TaskConflictException} with a suggested next available time if a conflict is found.
     *
     * @param taskId           the task being updated (excluded from the overlap check)
     * @param studentId        the student whose tasks are checked
     * @param newScheduledDate the requested scheduled date
     * @param durationMinutes  the estimated duration in minutes (may be null — defaults to 0)
     */
    private void checkScheduledDateConflict(String taskId, String studentId,
                                             LocalDateTime newScheduledDate,
                                             Integer durationMinutes) {
        int duration = durationMinutes != null ? durationMinutes : 0;
        LocalDateTime newEnd = newScheduledDate.plusMinutes(duration);

        List<Task> siblings = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> !t.getId().equals(taskId))
                .filter(t -> t.getScheduledDate() != null)
                .toList();

        for (Task other : siblings) {
            int otherDuration = other.getEstimatedDurationMinutes() != null
                    ? other.getEstimatedDurationMinutes() : 0;
            LocalDateTime otherEnd = other.getScheduledDate().plusMinutes(otherDuration);

            boolean overlaps = newScheduledDate.isBefore(otherEnd)
                    && newEnd.isAfter(other.getScheduledDate());
            if (overlaps) {
                LocalDateTime suggestion = siblings.stream()
                        .filter(t -> t.getScheduledDate() != null)
                        .map(t -> t.getScheduledDate().plusMinutes(
                                t.getEstimatedDurationMinutes() != null ? t.getEstimatedDurationMinutes() : 0))
                        .max(LocalDateTime::compareTo)
                        .orElse(newScheduledDate);
                throw new TaskConflictException(
                        "La fecha programada solapa con otra tarea. Fecha sugerida: " + suggestion);
            }
        }
    }
}
