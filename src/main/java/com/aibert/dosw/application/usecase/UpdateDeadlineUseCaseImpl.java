package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.UpdateDeadlineUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Spring service implementing the {@link UpdateDeadlineUseCase} input port (AIB-21 RN-02).
 * Called when the student drags a task to a new date on the interactive calendar:
 * persists the new deadline and recalculates urgency-based priority.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDeadlineUseCaseImpl implements UpdateDeadlineUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    private static final int DEADLINE_URGENCY_HOURS = 24;

    @Override
    public Task updateDeadline(String taskId, String studentId, LocalDateTime newDeadline) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getStudentId().equals(studentId)) {
            throw new TaskForbiddenException(taskId);
        }

        if (TaskStatus.COMPLETED.equals(task.getStatus())) {
            throw new TaskEditNotAllowedException(taskId);
        }

        task.setDeadline(newDeadline);

        // AIB-21 RN-02: recalculate urgency-based priority after deadline change
        long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), newDeadline);
        boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
        boolean canEscalate = task.getPriority() != TaskPriority.HIGH
                && task.getPriority() != TaskPriority.CRITICAL;
        if (isUrgent && canEscalate) {
            task.setPriority(TaskPriority.HIGH);
        }

        Task saved = taskRepositoryPort.save(task);
        log.info("AUDIT | operation=DEADLINE_UPDATE | studentId={} | taskId={} | newDeadline={}",
                studentId, taskId, newDeadline);
        return saved;
    }
}
