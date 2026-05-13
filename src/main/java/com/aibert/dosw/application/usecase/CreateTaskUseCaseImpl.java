package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Spring service implementing the {@link CreateTaskUseCase} input port.
 * Validates business rules (subject existence and duplicate check) before persisting the task.
 */
@Service
@RequiredArgsConstructor
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final SubjectValidationPort subjectValidationPort;

    private static final int DEADLINE_URGENCY_HOURS = 24;

    /**
     * {@inheritDoc}
     * <p>Execution order:</p>
     * <ol>
     *   <li>Verifies the subject exists via {@code SubjectValidationPort}.</li>
     *   <li>Checks for duplicate tasks (same student, subject, title).</li>
     *   <li>Sets {@code priority} to {@code MEDIUM} if none was provided.</li>
     *   <li>Escalates {@code priority} to {@code HIGH} if the deadline is within 24 h (RN1, RN2).</li>
     *   <li>Sets {@code status} to {@code TODO} if none was provided.</li>
     *   <li>Delegates persistence to {@code TaskRepositoryPort}.</li>
     * </ol>
     *
     * @param task the task to create
     * @return the persisted task
     * @throws SubjectNotFoundException if the referenced subject does not exist
     * @throws TaskConflictException    if a duplicate task already exists for the student
     */
    @Override
    public Task createTask(Task task) {
        if (!subjectValidationPort.exists(task.getSubjectId())) {
            throw new SubjectNotFoundException("La materia " + task.getSubjectId() + " no existe.");
        }

        if (taskRepositoryPort.existsDuplicate(task.getStudentId(), task.getSubjectId(), task.getTitle())) {
            throw new TaskConflictException("Ya existe una tarea con el título '" + task.getTitle() + "' para esta materia.");
        }

        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        // RN1 + RN2: escalate to HIGH when deadline is within the next 24 hours at creation time
        if (task.getDeadline() != null) {
            long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), task.getDeadline());
            boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
            boolean canEscalate = task.getPriority() != TaskPriority.HIGH
                    && task.getPriority() != TaskPriority.CRITICAL;
            if (isUrgent && canEscalate) {
                task.setPriority(TaskPriority.HIGH);
            }
        }

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        return taskRepositoryPort.save(task);
    }
}
