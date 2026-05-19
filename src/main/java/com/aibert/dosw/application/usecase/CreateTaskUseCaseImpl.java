package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.SubjectNotInActiveSemesterException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Spring service implementing the {@link CreateTaskUseCase} input port.
 * Validates business rules (subject existence, active semester, duplicate check) before persisting the task.
 * After creation, recalculates urgency-based priority for all active tasks of the student (AIB-18.1).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final SubjectValidationPort subjectValidationPort;

    private static final int DEADLINE_URGENCY_HOURS = 24;

    @Override
    public Task createTask(Task task) {
        // AIB-18.1: subject must exist
        if (!subjectValidationPort.exists(task.getSubjectId())) {
            throw new SubjectNotFoundException("La materia " + task.getSubjectId() + " no existe.");
        }

        // AIB-18.1 FA-03: subject must belong to the student's active semester (HTTP 422)
        if (!subjectValidationPort.isInActiveSemester(task.getSubjectId(), task.getStudentId())) {
            throw new SubjectNotInActiveSemesterException(task.getSubjectId());
        }

        if (taskRepositoryPort.existsDuplicate(task.getStudentId(), task.getSubjectId(), task.getTitle())) {
            throw new TaskConflictException("Ya existe una tarea con el título '" + task.getTitle() + "' para esta materia.");
        }

        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        // Escalate to HIGH when deadline is within the next 24 hours at creation time
        escalateIfUrgent(task);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        Task saved = taskRepositoryPort.save(task);
        log.info("AUDIT | operation=CREATE | studentId={} | taskId={} | title={} | createdAt={}",
                saved.getStudentId(), saved.getId(), saved.getTitle(), LocalDateTime.now());

        // AIB-18.1 note: recalculate priorities for all other active tasks of the student
        recalculatePriorityForActiveTasks(saved.getStudentId(), saved.getId());

        return saved;
    }

    private void escalateIfUrgent(Task task) {
        if (task.getDeadline() == null) return;
        long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), task.getDeadline());
        boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
        boolean canEscalate = task.getPriority() != TaskPriority.HIGH
                && task.getPriority() != TaskPriority.CRITICAL;
        if (isUrgent && canEscalate) {
            task.setPriority(TaskPriority.HIGH);
        }
    }

    private void recalculatePriorityForActiveTasks(String studentId, String excludeTaskId) {
        List<Task> activeTasks = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .filter(t -> !t.getId().equals(excludeTaskId))
                .toList();

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
